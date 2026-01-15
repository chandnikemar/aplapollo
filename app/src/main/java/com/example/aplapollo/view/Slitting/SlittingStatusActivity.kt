package com.example.aplapollo.view.Slitting

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Slitting.SlittingStatusAdapter
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.LogoutHelper
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionExpiredEvent
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.Slitting.HrSlittingTransactionRequest
import com.example.aplapollo.repository.APLRepository
import com.example.aplapollo.viewmodel.slittingstatus.SlittingStatusViewModel
import com.example.aplapollo.viewmodel.slittingstatus.SlittingStatusViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingStatusBinding
import es.dmoral.toasty.Toasty

class SlittingStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlittingStatusBinding
    private lateinit var progress: ProgressDialog
    private lateinit var slittingStatusViewModel: SlittingStatusViewModel
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var jobId: Int = 0
    private var barcode: String = ""
    private var supplierNo: String = ""
    private var tranPlanId:Int =0
    private var locationId:Int=0
    private var sourceStockId:Int=0

    private var motherWeight:Double=0.00
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting_status)
        binding.idLayoutHeader.tvTitle.text = "Slitting Status"

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        supportActionBar?.hide()
        session = SessionManager(this)
        userDetail = session.getUserDetails()

        val aplRepository = APLRepository()
        val viewModelProviderFactory = SlittingStatusViewModelfactory(application, aplRepository)
        slittingStatusViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingStatusViewModel::class.java]
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode= userDetail!![SessionManager.Key_tenantCode].toString()

            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        jobId = intent.getIntExtra("JOB_ID", 0)
        barcode = intent.getStringExtra("BARCODE") ?: ""
         motherWeight = intent.getStringExtra("Mother_Weight")?.toDoubleOrNull()!!

        binding.layoutScrapTable.etIronLoss.isEnabled = false


        // If supplier no is different key, replace accordingly
        supplierNo = barcode

        tranPlanId= intent.getIntExtra("HrSlitting_planID",0)
        sourceStockId=intent.getIntExtra("Source_StockID",0)
        locationId=intent.getIntExtra("Location_ID",0)

        SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
            if (shouldLogout == true) {
                SessionExpiredEvent.logoutLiveData.value = false
                LogoutHelper.handleLogout(this, session)
            }
        }

        val jobId = intent.getStringExtra("JOB_ID") ?: "--"
        val barcode = intent.getStringExtra("BARCODE") ?: "--"
//        val supplierNo = intent.getStringExtra("SupplierNo") ?: "--"
        val motherWeight =intent.getStringExtra("Mother_Weight")?:"--"
        binding.textJobNumber.text = "Job #$jobId"


        binding.tvMotherCoil.setText(barcode)
        binding.tvBatchNumber.setText("${motherWeight} Kg")


        slittingStatusViewModel.getHrSlittingDetailsById(baseUrl, tranPlanId)
        slittingStatusViewModel.hrSlittingDetailsLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val list = resource.data?.hRSlittingTransactionDetail ?: emptyList()

                    val adapter = SlittingStatusAdapter(list)
                    binding.recyclerSlitting.layoutManager = LinearLayoutManager(this)
                    binding.recyclerSlitting.adapter = adapter

                }

                is Resource.Error -> {
                    Toasty.error(this, resource.message ?: "Error").show()
                }

                else -> {}
            }
        }
        slittingStatusViewModel.completeHrSlittingLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()
                    Toasty.success(
                        this,
                        "HR Slitting completed successfully",
                        Toasty.LENGTH_SHORT
                    ).show()
                    finish() // or navigate back
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(
                        this,
                        resource.message ?: "Failed to complete slitting",
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
        binding.btnPrintBarcode.setOnClickListener {

//            printBarcodeLabel()
        }

        binding.btnSave.setOnClickListener {

            submitCompleteSlitting()
        }

    }
    private fun submitCompleteSlitting() {

        val adapter = binding.recyclerSlitting.adapter as SlittingStatusAdapter
        val detailsList = adapter.getUpdatedTransactionDetails()
        if (detailsList.isEmpty()) {
            Toasty.warning(this, "Please select at least one coil").show()
            return
        }
        // Mother coil weight (already received via intent)
        val motherCoilWeight = motherWeight

// Scrap weight from UI
        val scrapWeight = binding.layoutScrapTable.etScrapWeight
            .text.toString()
            .toDoubleOrNull() ?: 0.0

// Total child coil weight
        val totalChildWeight = detailsList.sumOf {
            it.weighAfterSlitting ?: 0.0
        }

// Iron Loss calculation
        val calculatedIronLoss =
            motherCoilWeight - (totalChildWeight + scrapWeight)

// Avoid negative value
        val ironLossWeight = if (calculatedIronLoss < 0) 0.0 else calculatedIronLoss


        binding.layoutScrapTable.etIronLoss.setText(
            String.format("%.2f", ironLossWeight)
        )

        val request = HrSlittingTransactionRequest(
            hrSlittingTranId = tranPlanId,
            tenantCode = tenantCode ?: "",
            locationId = locationId ,
            locationName = null,
            sourceStockId = sourceStockId,
            jobNumber = binding.textJobNumber.text
                .toString()
                .replace("Job #", ""),
            ironLossWeight = ironLossWeight,
            scrapWeight = scrapWeight,
            completedBy = userName,
            completedDate = null,
            status = "Completed",
            remarks = "Completed from app",
            totalRecord = detailsList.size,
            hRSlittingTransactionDetail = detailsList
        )

        slittingStatusViewModel.completeHrSlitting(baseUrl, request)
    }


}