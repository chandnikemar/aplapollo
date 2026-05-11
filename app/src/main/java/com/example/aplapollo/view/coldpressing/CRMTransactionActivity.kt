package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Coldpressing.CRMAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.CompleteStatus
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.model.CRM.CRMTransactionResponse
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmtransactionBinding
import es.dmoral.toasty.Toasty

class CRMTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmtransactionBinding
    private lateinit var progress: ProgressDialog
    private  lateinit var crmViewModel: CRMViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private lateinit var bomViewModel: BomViewModel
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

private var sourceStockId:Int=0
    private var barcode: String = ""
    private var motherBarcode: String = ""
    private var jobNumber:String=""
    private var motherWeight: Double = 0.0
    private var isCoilDivided: Boolean = false
    private lateinit var selectedProcess:String
    private lateinit var selectedMachineName:String
    private lateinit var grade:String
    private var isWeightErrorShown = false
    private var inputMaterialCode: String = ""
    private var inputBarcode:String=""
    private var bomOutputs: List<BomOutput> = emptyList()
    private var crmAdapter: CRMAdapter? = null
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
        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]
        val viewModelProviderFactor = BomInputCodeViewModelfactory(application, retrofitInstance)
        bomViewModel =
            ViewModelProvider(this, viewModelProviderFactor)[BomViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
//        locationId = intent.getIntExtra("LOCATION_ID", 0)
//        tranId=intent.getIntExtra("CRM_TRAN_JOB",0)
//        Log.d("RECEIVED_LOCATION", "Id=$locationId Name=$locationName")
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
            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"}

            // Disable until API loads
        binding.recyclerOutput.layoutManager = LinearLayoutManager(this)

//        crmAdapter = CRMAdapter(
//            list = mutableListOf(),
//            bomOutputs = emptyList(),
//            onWeightChanged = { calculateAndShowIronLoss() }
//        )

        binding.recyclerOutput.adapter = crmAdapter
        // Get Intent extras safely
        selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
       tranId=intent.getIntExtra("CRM_TRAN_JOB",0)
        inputBarcode=intent.getStringExtra(Constants.BarcodeValue)?:""
//        transactionId = intent.getIntExtra(Constants.PicklingId, 0)
        sourceStockId = intent.getIntExtra(Constants.SourceStockId, 0)
        locationId = intent.getIntExtra(LocationId, 0)
        grade= intent.getStringExtra(Constants.GradeV)?:""
        Log.d("LOCATION_DEBUG", "Received LocationId = $locationId")
        if (tranId == 0) {
            Toasty.error(this, "Invalid Transaction ID").show()
            finish()
            return
        }

            binding.btnSave.isEnabled = false
// Attach same watcher to both fields




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
                        val materialCode = resource?.data.materialCode ?: ""
                        inputMaterialCode = materialCode
                        binding.textInputMaterial.text = "$materialCode"

                        if (materialCode.isNotEmpty()) {
                            bomViewModel.getBom(materialCode)
                            Log.d("BOM_DEBUG", "MaterialCode = $materialCode")
                        }
                        crmAdapter?.updateList(listOf(transaction.barcode ?: ""))
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
                    val printRequestList = listOf(
                        PrintLabelBarcodeRequest(
                            barcode = barcode,
                            locationId = locationId,
                            createdDate = Utils.getCurrentDateTimeISO(),
                            createdBy = userName ?: ""
                        )
                    )

                    printlabelViewModel.printLabelBarcode(printRequestList)
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
        printlabelViewModel.barcodePrintLabelMutableLiveData.observe(this){ resource->
            when (resource) {
                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()

                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()

                }

                else -> {}
            }}
        bomViewModel.bomLiveData.observe(this) { response ->

            when (response) {

                is Resource.Success -> {
                    progress.dismiss()

                    val bomList = response.data ?: emptyList()

                    bomOutputs = bomList.flatMap { it.boMOutput }
                    crmAdapter?.updateList(listOf(barcode ?: ""))

                    crmAdapter?.setBomOutputs(bomOutputs)
                    // ✅ Step 1: ensure adapter has data
//                    if (crmAdapter?.getAllItems()?.isEmpty() == true && barcode.isNotEmpty()) {
//                        crmAdapter?.updateList(listOf(barcode))
//                    }

                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, response.message ?: "Error").show()
                }

                else -> {}
            }
        }
        // Listen to Scrap Weight

        binding.btnSave.setOnClickListener { submitCRM() }

    }
    private fun bindTransactionData(transaction: CRMTransactionResponse) {
        motherBarcode=transaction.motherBarcode
        binding.textJobNumber.text = "Job #${transaction.jobNumber}"
        binding.tvMotherCoil.setText(transaction.motherBarcode).toString()
        binding.tvBatchNumber.setText(transaction.motherCoilWeight.toString())
//        binding.tvBarcode?.setText(transaction.barcode)
        barcode = transaction.barcode ?: ""

        motherWeight = transaction.motherCoilWeight ?: 0.0

        tranId = transaction.crmTranId
        planId = transaction.crmPlanId
        tenantCode = transaction.tenantCode
        sourceStockId = transaction.sourceStockId
        jobNumber = transaction.jobNumber
//        binding.jobTable.editC4.isEnabled = true

        // ✅ ADD HERE
        crmAdapter?.updateList(listOf(barcode))
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




        val childWeight = crmAdapter?.getTotalWeight()?:0.0




        // Validation


        // Reset error
        isWeightErrorShown = false




        // Enable save only if valid
        binding.btnSave.isEnabled = true
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitCRM() {
        binding.recyclerOutput.adapter as CRMAdapter

        val enteredWeight =crmAdapter?.getTotalWeight()?:0.0



        val result = Utils.WeightValidationUtils.validateWeight(
            motherWeight = motherWeight,
            totalChildWeight = enteredWeight,
            scrapWeight = 0.0
        )

        if (result is Utils.WeightResult.Error) {
            Toasty.error(this, result.message).show()
            return
        }

        val ironLoss = (result as Utils.WeightResult.Success).ironLoss
        val outputMaterial = crmAdapter?.getSelectedOutputMaterial() ?: ""
        if (outputMaterial.isEmpty()) {
            Toasty.warning(this, "Please select Output Material").show()
            return
        }
        val request = CRMTransactionRequest(
            crmTranId = tranId,
            crmPlanId = planId,
            tenantCode = tenantCode,
            locationId = locationId,
            sourceStockId = sourceStockId ?: 0,
            desiredThickness=enteredWeight,
            weight=null,
            jobNumber = binding.textJobNumber.text .toString() .replace("Job #", ""),
            inputBarcode = motherBarcode,
            inputWeight = motherWeight.toString(),
            barcode = barcode,
            materialCode = outputMaterial,
            ironLossWeight = ironLoss,
            scrapWeight = 0.0,
            weightAfterCRM = enteredWeight,

            isCoilDivided = false,
            dividedCRMTranId = null,

            completedBy = userName ?: "",
            completedDate = Utils.getCurrentDateTimeISO(),

            status = CompleteStatus,
            remarks = "CRM Transaction",
            isPlanned = false,
            process=selectedProcess,
            machineName = selectedMachineName,
            tamper = "",
            grade=grade,
            component = crmAdapter?.getComponents()?: emptyList(),
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