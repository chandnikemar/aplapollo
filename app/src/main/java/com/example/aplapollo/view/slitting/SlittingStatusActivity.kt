package com.example.aplapollo.view.slitting

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Slitting.SlittingStatusAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
import com.example.aplapollo.model.Slitting.HrSlittingTransactionDetails
import com.example.aplapollo.model.Slitting.HrSlittingTransactionRequest
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
    private lateinit var slittingAdapter: SlittingStatusAdapter

    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private var tenantCode: String? = ""
    private var userDetail: HashMap<String, Any?>? = null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var jobId: Int = 0
    private var barcode: String = ""
    private var supplierNo: String = ""
    private var tranPlanId: Int = 0
    private var locationId: Int = 0
    private var sourceStockId: Int = 0

    private var motherWeight: Double = 0.00
    private var scrapWeight: Double = 0.0
    private var ironLossWeight: Double = 0.0
    private var isWeightErrorShown = false
    private val weightMap = mutableMapOf<Int, Double>()


    @RequiresApi(Build.VERSION_CODES.O)
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

        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactory = SlittingStatusViewModelfactory(application, retrofitInstance)
        slittingStatusViewModel =
            ViewModelProvider(this, viewModelProviderFactory)[SlittingStatusViewModel::class.java]
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode = userDetail!![SessionManager.Key_tenantCode].toString()

            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code", "Tenant Code= $tenantCode")
        }
        jobId = intent.getIntExtra("JOB_ID", 0)
        barcode = intent.getStringExtra("BARCODE") ?: ""
        motherWeight = intent.getStringExtra("Mother_Weight")
            ?.toDoubleOrNull() ?: 0.0

        binding.layoutScrapTable.tvIronLossValue.isEnabled = false
        supplierNo = barcode

        tranPlanId = intent.getIntExtra("HrSlitting_planID", 0)
        sourceStockId = intent.getIntExtra("Source_StockID", 0)
        locationId = intent.getIntExtra("Location_ID", 0)


        val jobId = intent.getStringExtra("JOB_ID") ?: "--"
        val barcode = intent.getStringExtra("BARCODE") ?: "--"
//        val supplierNo = intent.getStringExtra("SupplierNo") ?: "--"
        motherWeight = intent.getStringExtra("Mother_Weight")
            ?.toDoubleOrNull() ?: 0.0


        binding.textJobNumber.text = "Job #$jobId"


        binding.tvMotherCoil.setText(barcode)
        binding.tvBatchNumber.setText("${motherWeight} Kg")


        slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)
        binding.layoutScrapTable.etScrapWeight.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun afterTextChanged(s: android.text.Editable?) {
                    calculateAndShowIronLoss()
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            }
        )


        slittingStatusViewModel.hrSlittingDetailsLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    val list = resource.data?.hRSlittingTransactionDetail ?: emptyList()

                    slittingAdapter = SlittingStatusAdapter(list) {
                        calculateAndShowIronLoss()
                    }
                    binding.recyclerSlitting.layoutManager =
                        LinearLayoutManager(this)

                    binding.recyclerSlitting.adapter = slittingAdapter

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitCompleteSlitting() {

        val adapter = slittingAdapter

        val detailsList = adapter.getUpdatedTransactionDetails()

        if (detailsList.isEmpty()) {
            Toasty.warning(this, "Please select at least one coil").show()
            return
        }

        if (detailsList.any { it.weighAfterSlitting ?: 0.0 <= 0 }) {
            Toasty.error(this, "Enter valid Child Weight").show()
            return
        }

        if (scrapWeight <= 0) {
            Toasty.warning(this, "Enter Scrap Weight").show()
            return
        }

        if (ironLossWeight <= 0) {
            Toasty.warning(this, "Invalid Iron Loss").show()
            return
        }

        val totalChildWeight =
            detailsList.sumOf { it.weighAfterSlitting ?: 0.0 }

        if (totalChildWeight + scrapWeight > motherWeight) {

            Toasty.error(this, "Weight exceeds Mother Coil").show()
            return
        }


        // ✅ Convert to API Model
        val apiDetailsList = detailsList.map { item ->

            HrSlittingTransactionDetails(

                hrSlittingTranDtlId = item.hrSlittingTranDtlId,
                hrSlittingTranId = item.hrSlittingTranId,
                width = item.width ?: 0.0,
                barcode = item.barcode ?: "",

                weighAfterSlitting = item.weighAfterSlitting ?: 0.0,

                weightTakenBy = userName,
                weightLocationId = locationId,
                weightDatetime = getCurrentDateTimeISO(),

                status = "Completed",
                isActive = true,

                createdBy = userName ?: "",
                createdDate = getCurrentDateTimeISO(),

                modifiedBy = null,
                modifiedDate = null,

                tenantCode = tenantCode,
                tenantGroupCode = null
            )
        }


        val request = HrSlittingTransactionRequest(

            hrSlittingTranId = tranPlanId,
            tenantCode = tenantCode ?: "",
            locationId = locationId,
            locationName = null,
            sourceStockId = sourceStockId,

            jobNumber = binding.textJobNumber.text
                .toString()
                .replace("Job #", ""),
            ironLossWeight = ironLossWeight,
            scrapWeight = scrapWeight,
            completedBy = userName,
            completedDate = getCurrentDateTimeISO(),
            status = "Completed",
            remarks = "Completed from app",
            totalRecord = apiDetailsList.size,
            hrSlittingTransactionDetail = apiDetailsList
        )


        slittingStatusViewModel.completeHrSlitting(request)
    }


    private fun calculateAndShowIronLoss() {

        val adapter = slittingAdapter

        val detailsList = adapter.getUpdatedTransactionDetails()

        val totalChildWeight = detailsList.sumOf {
            it.weighAfterSlitting ?: 0.0
        }

        val scrapWeightInput = binding.layoutScrapTable.etScrapWeight
            .text.toString()
            .toDoubleOrNull() ?: 0.0


        // Scrap + Child <= Mother
        if ((scrapWeightInput + totalChildWeight) > motherWeight) {

            if (!isWeightErrorShown) {
                Toasty.error(
                    this,
                    "Total weight exceeds Mother Coil Weight"
                ).show()

                isWeightErrorShown = true
            }

            binding.layoutScrapTable.tvIronLossValue.setText("0.00")

            return
        } else {
            // Reset flag when valid
            isWeightErrorShown = false
        }


        // ✅ Calculate Iron Loss
        val calculatedIronLoss =
            motherWeight - (totalChildWeight + scrapWeightInput)

        val ironLoss =
            if (calculatedIronLoss < 0) 0.0 else calculatedIronLoss


        ironLossWeight = ironLoss
        scrapWeight = scrapWeightInput


        // ✅ Show Iron Loss
        binding.layoutScrapTable.tvIronLossValue.setText(
            String.format("%.2f", ironLoss)
        )
    }
}


