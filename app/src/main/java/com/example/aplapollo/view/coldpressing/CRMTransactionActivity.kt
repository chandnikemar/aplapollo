package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.WeightResult
import com.example.aplapollo.helper.WeightValidationUtils
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.model.CRM.CRMTransactionResponse
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmtransactionBinding
import es.dmoral.toasty.Toasty

class CRMTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmtransactionBinding
    private lateinit var progress: ProgressDialog
    private  lateinit var crmViewModel: CRMViewModel
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var tranId: Int = 0
    private var planId:Int=0
    private var locationId: Int = 0
    private var locationName: String = ""
private var sourceStockId:Int=0
    private var barcode: String = ""
    private var jobNumber:String=""
    private var motherWeight: Double = 0.0
    private var isCoilDivided: Boolean = false
    private var isWeightErrorShown = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmtransaction)
        binding.idLayoutHeader.tvTitle.text = "CRN Transaction"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        binding.idLayoutHeader.tvTitle.text = "CRM Plan"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)

        val viewModelProviderFactory = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, viewModelProviderFactory)[CRMViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        locationId = intent.getIntExtra("LOCATION_ID", 0)
        locationName = intent.getStringExtra("LOCATION_NAME") ?: ""
        tranId=intent.getIntExtra("CRM_TRAN_JOB",0)
        Log.d("RECEIVED_LOCATION", "Id=$locationId Name=$locationName")
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
            tranId = intent.getIntExtra("CRM_TRAN_JOB", 0)
            locationId = intent.getIntExtra("LOCATION_ID", 0)
            // Disable until API loads
            binding.jobTable.editC4.isEnabled = false
            binding.layoutScrapTable.etScrapWeight.isEnabled = false

            binding.layoutScrapTable.tvIronLossValue.isEnabled = false
            binding.btnSave.isEnabled = false
// Attach same watcher to both fields
            binding.layoutScrapTable.etScrapWeight.addTextChangedListener(weightWatcher)

            binding.jobTable.editC4.addTextChangedListener(weightWatcher)

            crmViewModel.getCRMPlanTranDetailById(tranId)
            crmViewModel.CrmPlanTranDetailLiveData.observe(this) { resource ->
                when (resource) {
                    is Resource.Loading -> showProgress()
                    is Resource.Success -> {
                        dismissProgress()
                        val transaction = resource.data
                        if (transaction == null) {
                            Toasty.error(this, "No transaction data found").show()
                            return@observe
                        }

                        bindTransactionData(transaction)
                        binding.btnSave.isEnabled = true

                    }
                    is Resource.Error -> {
                        dismissProgress()
                        Toasty.error(this, resource.message ?: "Error fetching transaction").show()
                    }

                    else -> {}
                }
            }
        }
        crmViewModel.ProcessCRMLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()

                    val response = resource.data
//                    val tranId = response?.responseObject.
//                    val jobNo = response?.responseObject?.jobNumber

                    Toasty.success(
                        this,
                        response?.responseMessage ?: "CRM initiated",
                        Toasty.LENGTH_SHORT
                    ).show()

//                    Log.d("SLITTING_INIT", "TranId=$tranId JobNo=$jobNo")
                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(
                        this,
                        resource.message ?: "Failed to initiate CRM",
                        Toasty.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
        // Listen to Scrap Weight

        binding.btnSave.setOnClickListener { submitCRM() }

    }
    private fun bindTransactionData(transaction: CRMTransactionResponse) {

        binding.textJobNumber.text = "Job #${transaction.jobNumber}"
        binding.tvMotherCoil.setText(transaction.motherBarcode)
        binding.tvBatchNumber.setText(transaction.motherCoilWeight.toString())

        binding.jobTable.textC2?.setText(transaction.barcode)

        barcode = transaction.barcode ?: ""

        // ✅ IMPORTANT
        motherWeight = transaction.motherCoilWeight ?: 0.0

        tranId = transaction.crmTranId
        planId = transaction.crmPlanId
        tenantCode = transaction.tenantCode
        sourceStockId = transaction.sourceStockId
        jobNumber = transaction.jobNumber

        // ✅ Enable inputs after load
        binding.jobTable.editC4.isEnabled = true
        binding.layoutScrapTable.etScrapWeight.isEnabled = true


        // ✅ Recalculate after data load
        calculateAndShowIronLoss()
    }
    private val weightWatcher = object : android.text.TextWatcher {

        override fun afterTextChanged(s: android.text.Editable?) {
            calculateAndShowIronLoss()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun calculateAndShowIronLoss() {

        // Wait until API loads
        if (motherWeight <= 0) {
            binding.layoutScrapTable.tvIronLossValue?.setText("0.00")
            return
        }

        val scrapWeight = binding.layoutScrapTable.etScrapWeight
            ?.text.toString().toDoubleOrNull() ?: 0.0

        val childWeight = binding.jobTable.editC4
            ?.text.toString().toDoubleOrNull() ?: 0.0


        val totalUsedWeight = scrapWeight + childWeight

        // Validation
        if (totalUsedWeight > motherWeight) {

            if (!isWeightErrorShown) {
                Toasty.error(
                    this,
                    "Child + Scrap weight cannot exceed Mother weight"
                ).show()

                isWeightErrorShown = true
            }

            binding.layoutScrapTable.tvIronLossValue?.setText("0.00")
            binding.btnSave.isEnabled = false
            return
        }

        // Reset error
        isWeightErrorShown = false


        // ✅ Calculate remaining (Iron Loss)
        val ironLoss = motherWeight - totalUsedWeight

        binding.layoutScrapTable.tvIronLossValue?.setText(
            String.format("%.2f", ironLoss)
        )

        // Enable save only if valid
        binding.btnSave.isEnabled = true
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitCRM() {

        val scrapWeight =
            binding.layoutScrapTable.etScrapWeight
                ?.text.toString().toDoubleOrNull() ?: 0.0

        val enteredWeight =
            binding.jobTable.editC4
                ?.text.toString().toDoubleOrNull() ?: 0.0


        val result = WeightValidationUtils.validateWeight(
            motherWeight = motherWeight,
            totalChildWeight = enteredWeight,
            scrapWeight = scrapWeight
        )

        if (result is WeightResult.Error) {
            Toasty.error(this, result.message).show()
            return
        }

        val ironLoss = (result as WeightResult.Success).ironLoss


        val request = CRMTransactionRequest(
            crmTranId = tranId,
            crmPlanId = planId,
            tenantCode = tenantCode,
            locationId = locationId,
            sourceStockId = sourceStockId ?: 0,
            desiredThickness=enteredWeight,
            jobNumber = jobNumber,
            barcode = barcode,

            ironLossWeight = ironLoss,
            scrapWeight = scrapWeight,
            weightAfterCRM = enteredWeight,

            isCoilDivided = false,
            dividedCRMTranId = null,

            completedBy = userName ?: "",
            completedDate = Utils.getCurrentDateTimeISO(),

            status = "Completed",
            remarks = "CRM Transaction",
            isPlanned = false
        )


        crmViewModel.processCRM(request)
    }

    private fun showProgress() {
        if (!progress.isShowing) progress.show()
    }


    private fun dismissProgress() {
        if (::progress.isInitialized && progress.isShowing) progress.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgress()
    }
}