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
    private var locationId:Int=0
    private var sourceStockId: Int = 0
    private var scannedBarcode: String? = null
    private  var tenantCode:String?=null
    private var transactionId:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling_inward)
        binding.idLayoutHeader.tvTitle.text = "HR Pickling Input"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        val viewModelProviderFactoryPickling = PicklingViewModelfactory(application, retrofitInstance)
        picklingViewModel = ViewModelProvider(this, viewModelProviderFactoryPickling)[PicklingViewModel::class.java]
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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

                    binding.inCommanBatch.tvItemCode.text =
                        "Item Code : ${data?.materialCode}"

                    binding.inCommanBatch.tvGrade.text =
                        "Grade : ${data?.grade}"
                    binding.inCommanBatch.tvLength.text =
                        "Length : ${data?.length}"

                    binding.inCommanBatch.tvWidth.text =
                        "Width : ${data?.width} MM"

                    binding.inCommanBatch.tvThickness.text =
                        "Thickness : ${data?.thickness} MM"

                    binding.inCommanBatch.tvWeight.text =
                        "Weight : ${data?.weight} KG"

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
                tenantCode =tenantCode ?: "",
                locationId = locationId,
                sourceStockId = sourceStockId,
                jobNumber = "",
                barcode = scannedBarcode?:"",
                ironLossWeight = null,
                scrapWeight = null,
                weightAfterPickling = null,
                completedBy = "",
                completedDate ="",
                status = "",
                remarks = "Pickling Proccess",
                isDivided = false
            )

            picklingViewModel.submitPickling(request)
           Log.d("PICKLING_POST", "Request Body = $request")
        }

    }
//    override fun onDestroy() {
//        super.onDestroy()
//        if (::progress.isInitialized && progress.isShowing) {
//            progress.dismiss()
//        }
//    }

}