package com.example.aplapollo.view.Pickling

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.view.BaseScanActivity
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanViewModelfactory
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanvViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingInwardBinding
import es.dmoral.toasty.Toasty

class PicklingInwardActivity : BaseScanActivity() {
    private lateinit var binding:ActivityPicklingInwardBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private  lateinit var  picklingViewModel: PicklingViewModel
    private lateinit var slittingWithoutplanvViewModel: SlittingWithoutplanvViewModel
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var locationId: Int = 0
    private var locationName: String = ""
    private var sourceStockId: Int = 0
    private var scannedBarcode: String? = null
    private  var tenantCode:String?=null
    private var transactionId:Int=0
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    override fun onBarcodeScanned(barcode: String) {

        runOnUiThread {

            Log.d("SCAN_DEBUG", "Scanned Barcode = $barcode")

            // Show scanned value in EditText
            binding.commanInputRow.inputField.setText(barcode)

            // Move cursor to end
            binding.commanInputRow.inputField.setSelection(barcode.length)

            // Save barcode
            scannedBarcode = barcode

            // Call API automatically

        }
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling_inward)
        binding.commanInputRow.inputField.apply {

            requestFocus()

            isFocusable = true
            isFocusableInTouchMode = true

            post {
                requestFocus()

            }
        }
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        val viewModelProviderFactoryPickling = PicklingViewModelfactory(application, retrofitInstance)
        picklingViewModel = ViewModelProvider(this, viewModelProviderFactoryPickling)[PicklingViewModel::class.java]
        val viewModelProviderFactory = SlittingWithoutplanViewModelfactory(application, retrofitInstance)
        slittingWithoutplanvViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingWithoutplanvViewModel::class.java]
        binding.idLayoutHeader.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed()}
            if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode= userDetail!!["defaultTenantCode"].toString()

            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }

        locationId = intent.getIntExtra(LocationId, 0)
        Log.d("Tanent_Code","Tenant Code= $locationId")
        selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
        binding.idLayoutHeader.tvTitle.text = "$selectedProcessName Planning"
        binding.idLayoutHeader.tvSubtitle.text="Generate production plans from scanned coils"
        binding.layoutBatchDetails.visibility = View.GONE
            slittingWithoutplanvViewModel.stockByBarcodeLiveData.observe(this) { resource ->

            when (resource) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {


                    progress.dismiss()
                    val data = resource.data ?: return@observe
                    Log.d("BARCODE", data.toString())
                    binding.layoutBatchDetails.visibility = View.VISIBLE
                    binding.layoutActionButtons.visibility=View.VISIBLE
                    binding.inPicklingBatch.tvItemCode.text =
                        data?.materialCode ?: ""

                    binding.inPicklingBatch.tvGrade.text =
                        "${data?.grade ?: "-"}"

                    binding.inPicklingBatch.tvSupplierBatchNo.text =
                        "${data?.supplierBatchNo ?: "-"}"

                    binding.inPicklingBatch.tvWidth.text =
                        "%.3f".format(data?.width.toString().toDoubleOrNull() ?: 0.0)

                    binding.inPicklingBatch.tvThickness.text =
                        "%.2f".format(data?.thickness.toString().toDoubleOrNull() ?: 0.0)

                    binding.inPicklingBatch.tvWeight.text =
                        "%.3f".format(data?.weight.toString().toDoubleOrNull() ?: 0.0)

                    sourceStockId = data?.stockId!!
                    scannedBarcode = data?.barcode
                    tenantCode=data?.tenantCode
                    transactionId=data?.transactionId?:0
                }
                is Resource.Error -> {
                    progress.dismiss()
                    Utils.showErrorDialog(this, resource.message ?: "Error",)
//                    Toasty.error(
//                        this,
//                        resource.message ?: "Invalid barcode",
//                        Toasty.LENGTH_SHORT
//                    ).show()

                }

                else -> {}
            }

        }
        picklingViewModel.processPicklingLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    Toast.makeText(
                        this,
                        result.data,
                        Toast.LENGTH_LONG
                    ).show()

                    finish() // optional
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }

        binding.commanInputRow.btnSearch.setOnClickListener {

            val barcode = binding.commanInputRow.inputField.text.toString().trim()

            if (barcode.isEmpty()) {
                Toasty.warning(this, "Please scan barcode").show()
                return@setOnClickListener
            }
            slittingWithoutplanvViewModel
                .getStockByBatchOrBarcode( barcode)
        }
        binding.commanInputRow.btnClear.setOnClickListener {

            // Clear input field
            binding.commanInputRow.inputField.setText("")

            // Hide data card
            binding.layoutBatchDetails.visibility = View.GONE

            // Reset values
            scannedBarcode = null
            sourceStockId = 0
            transactionId = 0

//            Toasty.info(this, "Input cleared").show()
        }
        binding.btncClears.setOnClickListener {

            // Clear input field also (optional but recommended)
            binding.commanInputRow.inputField.setText("")

            // Clear UI data
            binding.inPicklingBatch.tvItemCode.text = ""
            binding.inPicklingBatch.tvGrade.text = ""
            binding.inPicklingBatch.tvSupplierBatchNo.text = ""
            binding.inPicklingBatch.tvWidth.text = ""
            binding.inPicklingBatch.tvThickness.text = ""
            binding.inPicklingBatch.tvWeight.text = ""

            // Hide card
            binding.layoutBatchDetails.visibility = View.GONE

            // Reset variables
            scannedBarcode = null
            sourceStockId = 0
            transactionId = 0


        }
       binding.btncSaves.setOnClickListener {
           if (locationId == 0) {
               Toasty.warning(this, "Location is missing").show()
               return@setOnClickListener
           }

           if (scannedBarcode == null || transactionId == 0) {

               Toasty.warning(
                   this,
                   "Please scan barcode first"
               ).show()

               return@setOnClickListener
           }

            val request = ProcessPicklingRequest(
                picklingTranId = 0,
                tenantCode =tenantCode,
                locationId = locationId,
                sourceStockId = sourceStockId,
                jobNumber = "",
                status = "InProgress",
                remarks = "Pickling Proccess",
                isDivided = false,
                process = selectedProcessName,
                IsActive =true,

            )

            picklingViewModel.submitPickling(request)
           Log.d("PICKLING_POST", "Request Body = $request")
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        if (::progress.isInitialized && progress.isShowing) {
            progress.dismiss()
        }
    }

}