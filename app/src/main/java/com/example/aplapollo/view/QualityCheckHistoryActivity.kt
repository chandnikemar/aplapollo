package com.example.aplapollo.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.QcHistoryAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.ZebraPrinterHelper
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.aplapollo.viewmodel.qualitycheck.QCViewModel
import com.example.aplapollo.viewmodel.qualitycheck.QcViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityQualityCheckHistoryBinding
import es.dmoral.toasty.Toasty

class QualityCheckHistoryActivity : AppCompatActivity() {
    private  lateinit var binding:ActivityQualityCheckHistoryBinding
    private lateinit var qcviewModel: QCViewModel
    private lateinit var qcPrintLabelViewModel: PrintlabelViewModel
    private lateinit var qcHistoryAdapter: QcHistoryAdapter

    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var tenantCode: String
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""

    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var selectedMaterialTypeId: Int = 0
    private var qcStatus: String = ""
    private var scanBuffer = StringBuilder()
    private var lastKeyTime = 0L
    private val SCAN_TIMEOUT = 300L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quality_check_history)
        binding.idLayoutHeader.tvTitle.text = "Quality Check History "
        binding.idLayoutHeader.tvSubtitle.text = "Track QC records and print labels again"

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactory = QcViewModelFactory(application, retrofitInstance)
        qcviewModel = ViewModelProvider(this, viewModelProviderFactory)[QCViewModel::class.java]
        val printlabelviewModelProviderFactory=
            QcprintlabelViewModelFactory(application,retrofitInstance)
        qcPrintLabelViewModel = ViewModelProvider(this,printlabelviewModelProviderFactory)[PrintlabelViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode = userDetail!![SessionManager.Key_tenantCode]?.toString() ?: ""


            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


        }
        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )

        binding.btnNewQC.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    QualityCheckActivity::class.java
                )
            )
        }
        loadQcHistory()
        qcHistoryAdapter = QcHistoryAdapter { selectedQc ->

            val request = PrintLabelRequest(
                SupplierName = selectedQc.supplierName ?: "",
                BarCode = selectedQc.barcode ?: "",
                SupplierBatchNo = selectedQc.supplierBatchNo ?: "",
                MaterialCode = selectedQc.materialCode ?: "",
                Grade = selectedQc.grade ?: "",
                Thickness = selectedQc.thickness ?: 0.0,
                Width = selectedQc.width ?: 0.0,
                GRNNumber = selectedQc.grnNo ?: "",
                GRNDate = selectedQc.grnDate ?: "",
                NetWeight = selectedQc.netWeight ?: 0.0,
                CreatedBy = userName ?: "",
                CreatedDate = Utils.todayDate
            )

            Log.d("QC_PRINT", "Request = $request")

            qcPrintLabelViewModel.printQcLabel(request)
        }

        binding.rvQcHistory.apply {
            layoutManager = LinearLayoutManager(this@QualityCheckHistoryActivity)
            adapter = qcHistoryAdapter
        }


        qcviewModel.qcTransactionMutableLiveData.observe(this) { resource ->

            when (resource) {

                is Resource.Loading<*> -> {
                    progress.show()
                }

                is Resource.Success<*> -> {

                    progress.dismiss()

                    val qcList = resource.data ?: emptyList()

                    Log.d("QC_LIST", qcList.toString())
                    if (qcList.isEmpty()) {

                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvQcHistory.visibility = View.GONE

                    } else {

                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvQcHistory.visibility = View.VISIBLE

                        qcHistoryAdapter.submitList(qcList)
                    }
                }

                is Resource.Error<*> -> {

                    progress.dismiss()
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvQcHistory.visibility = View.GONE

                    Toasty.error(
                        this,
                        resource.message ?: "Failed to load data",
                        Toasty.LENGTH_SHORT
                    ).show()
                }
            }
        }
        qcPrintLabelViewModel.qcPrintMutableLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    Log.d("QC_PRINT", "API call loading")
                }

                is Resource.Success -> {

                    val zpl = result.data?.responseObject
                    Log.d("QC_PRINT", "ZPL = $zpl")

                    if (zpl.isNullOrEmpty()) {
                        Toast.makeText(this, "ZPL not received", Toast.LENGTH_SHORT).show()
                        return@observe
                    }


                    val printerMac = Utils.getSharedPrefs(this,Constants.KEY_PRINTER_MAC)
                    Log.d("QC_PRINT", "Printer MAC = $printerMac")

                    if (printerMac.isNullOrEmpty()) {
                        Toast.makeText(this, "Printer not configured", Toast.LENGTH_SHORT).show()
                        return@observe
                    }

                    ZebraPrinterHelper.printViaService(
                        context = this,
                        mac = printerMac,
                        zpl = zpl
                    )

                    Toast.makeText(this, "Printing started", Toast.LENGTH_SHORT).show()
                    goToHome()
                }

                is Resource.Error -> {
                    Log.e("QC_PRINT", "API error: ${result.message}")

                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }

                else -> Unit
            }
        }

    }
    private fun loadQcHistory() {

        qcviewModel.getAllQcTransaction(
        )
    }
    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}