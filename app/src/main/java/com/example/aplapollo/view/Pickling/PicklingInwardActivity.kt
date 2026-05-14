package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingInwardBinding
import es.dmoral.toasty.Toasty

class PicklingInwardActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPicklingInwardBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private  lateinit var  picklingViewModel: PicklingViewModel
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling_inward)
        binding.idLayoutHeader.tvTitle.text = ""
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        val viewModelProviderFactoryPickling = PicklingViewModelfactory(application, retrofitInstance)
        picklingViewModel = ViewModelProvider(this, viewModelProviderFactoryPickling)[PicklingViewModel::class.java]
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
        binding.idLayoutHeader.tvTitle.text = selectedProcessName+"INWARD"
        binding.layoutBatchDetails.visibility = View.GONE

        picklingViewModel.picklingBarcodeLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {


                    progress.dismiss()
                    val data = result.data?.responseObject
                    Log.d("BARCODE", data.toString())
                    binding.layoutBatchDetails.visibility = View.VISIBLE

                    binding.inPicklingBatch.tvItemCode.text =
                        data?.materialCode ?: ""

                    binding.inPicklingBatch.tvGrade.text =
                        "${data?.grade ?: "-"}"

                    binding.inPicklingBatch.tvSupplierBatchNo.text =
                        "${data?.supplierBatchNo ?: "-"}"

                    binding.inPicklingBatch.tvWidth.text =
                        "${data?.width ?: 0}"

                    binding.inPicklingBatch.tvThickness.text =
                        "${data?.thickness ?: 0}"

                    binding.inPicklingBatch.tvWeight.text =
                        "${data?.weight ?: 0}"
                    sourceStockId = data?.stockId!!
                    scannedBarcode = data?.barcode
                    tenantCode=data?.tenantCode
                    transactionId=data?.transactionId?:0
                    Toasty.success(this, "Stock fetched successfully").show()
                }

                is Resource.Error -> {
                    progress.dismiss()

                    Toasty.error(
                        this,
                        result.message ?: "Invalid barcode",
                        Toasty.LENGTH_SHORT
                    ).show()

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
            picklingViewModel
                .fetchPicklingBarcodeData( barcode)
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

            Toasty.info(this, "Input cleared").show()
        }
        binding.btnbClear.setOnClickListener {

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

            Toasty.info(this, "Data cleared").show()
        }
       binding.btnSave.setOnClickListener {
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
                picklingTranId = transactionId,
                tenantCode =tenantCode,
                locationId = locationId,
                sourceStockId = sourceStockId,
                jobNumber = "",
                status = "",
                remarks = "Pickling Proccess",
                isDivided = false,
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