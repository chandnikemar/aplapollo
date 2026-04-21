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
import com.example.aplapollo.adapter.BomAdapter
import com.example.aplapollo.adapter.Slitting.SlittingStatusAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Constants.ChildMotherExceedError
import com.example.aplapollo.helper.Constants.CompleteStatus
import com.example.aplapollo.helper.Constants.EnterScrapWeight
import com.example.aplapollo.helper.Constants.GradeV
import com.example.aplapollo.helper.Constants.HrSlittingId
import com.example.aplapollo.helper.Constants.HrSlittingPlanId
import com.example.aplapollo.helper.Constants.InvalidIronLoss
import com.example.aplapollo.helper.Constants.JobId
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Constants.MotherWeightV
import com.example.aplapollo.helper.Constants.SelectCoil
import com.example.aplapollo.helper.Constants.SourceStockId
import com.example.aplapollo.helper.Constants.ValidChildWeightError
import com.example.aplapollo.helper.Constants.WeightExceed
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
import com.example.aplapollo.model.BoMMasterResponse
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.model.Slitting.ComponentRequest
import com.example.aplapollo.model.Slitting.HrSlittingRequest
import com.example.aplapollo.model.Slitting.HrSlittingTransactionDetails
import com.example.aplapollo.model.Slitting.InputRequest
import com.example.aplapollo.model.Slitting.OutputRequest
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.aplapollo.viewmodel.slittingstatus.SlittingStatusViewModel
import com.example.aplapollo.viewmodel.slittingstatus.SlittingStatusViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingStatusBinding
import es.dmoral.toasty.Toasty

class SlittingStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlittingStatusBinding
    private lateinit var progress: ProgressDialog
    private lateinit var slittingStatusViewModel: SlittingStatusViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private lateinit var bomViewModel:BomViewModel
    private lateinit var session: SessionManager
    private lateinit var slittingAdapter: SlittingStatusAdapter
    private lateinit var bomAdapter: BomAdapter

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
    private var hrSlittingPlanId=0

    private var motherWeight: Double = 0.00
    private var scrapWeight: Double = 0.0
    private var ironLossWeight: Double = 0.0
    private var isWeightErrorShown = false

    private var bomList: List<BoMMasterResponse> = emptyList()

    private var selectedInputMaterial: String = ""
    private lateinit var selectedProcess:String
    private lateinit var selectedMachineName:String
    private lateinit var grade:String

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
        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]

        val viewModelProviderFactor = BomInputCodeViewModelfactory(application, retrofitInstance)
        bomViewModel =
            ViewModelProvider(this, viewModelProviderFactor)[BomViewModel::class.java]
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
//            // PRINT TOKEN HERE
//            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
//            Log.d("Tanent_Code", "Tenant Code= $tenantCode")
        }


        binding.layoutScrapTable.tvIronLossValue.isEnabled = false
        supplierNo = barcode

        tranPlanId = intent.getIntExtra(HrSlittingId, 0)
        sourceStockId = intent.getIntExtra(SourceStockId, 0)
        locationId = intent.getIntExtra(LocationId, 0)
grade= intent.getStringExtra(GradeV)?:""
       hrSlittingPlanId=intent.getIntExtra(HrSlittingPlanId,0)
        selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
        val jobId = intent.getStringExtra(JobId) ?: "--"
        val barcode = intent.getStringExtra(BarcodeValue) ?: "--"
//        val supplierNo = intent.getStringExtra("SupplierNo") ?: "--"
        motherWeight = intent.getStringExtra(MotherWeightV)
            ?.toDoubleOrNull() ?: 0.0


        binding.textJobNumber.text = "Job #$jobId"


        binding.tvMotherCoil.setText(barcode)
        binding.tvBatchNumber.setText("${motherWeight} Kg")

        val inputCode = binding.tvMotherCoil.text.toString()
        if (inputCode.isNotEmpty()) {
            bomViewModel.getBom(inputCode)
        }



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
                    val adapter = slittingAdapter
                    val updatedList = adapter.getUpdatedTransactionDetails()
                    val printRequestList = updatedList.map { item ->
                        PrintLabelBarcodeRequest(
                            barcode = item.barcode ?: "",
                            locationId = locationId,
                            createdDate = getCurrentDateTimeISO() ,
                            createdBy =userName.toString()
                        )
                    }

                    printlabelViewModel.printLabelBarcode(printRequestList)
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
        bomViewModel.bomMutableLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    bomList = resource.data ?: emptyList()

                    bomAdapter = BomAdapter(bomList)

                    binding.recyclerBomDetails.layoutManager = LinearLayoutManager(this)
                    binding.recyclerBomDetails.adapter = bomAdapter
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.warning(this, resource.message ?: "Error").show()
                }

                else -> {}
            }
        }
        binding.dropdownBom.setOnItemClickListener { _, _, position, _ ->

            val selected = bomList[position]
            selectedInputMaterial = selected.inputMaterial

            val adapter = BomAdapter(listOf(selected))

            binding.recyclerBomDetails.adapter = adapter
        }
        binding.btnPrintBarcode.setOnClickListener {


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
            Toasty.warning(this, SelectCoil).show()
            return
        }

        if (detailsList.any { it.weighAfterSlitting ?: 0.0 <= 0 }) {
            Toasty.error(this, ValidChildWeightError).show()
            return
        }

        if (scrapWeight <= 0) {
            Toasty.warning(this, EnterScrapWeight).show()
            return
        }

        if (ironLossWeight <= 0) {
            Toasty.warning(this, InvalidIronLoss).show()
            return
        }

        val totalChildWeight =
            detailsList.sumOf { it.weighAfterSlitting ?: 0.0 }

        if (totalChildWeight + scrapWeight > motherWeight) {

            Toasty.error(this, WeightExceed).show()
            return
        }
        val bomData = getSelectedBomData()

        bomData.forEach {
            Log.d("FINAL_INPUT", it.MaterialCode + " - " + it.Weight)

            it.Outputs.forEach { output ->
                Log.d("FINAL_OUTPUT", output.MaterialCode + " - " + output.Weight)

                output.Component.forEach { comp ->
                    Log.d("FINAL_COMPONENT", comp.MaterialCode + " - " + comp.Weight)
                }
            }
        }

        //  Convert to API Model
        val apiDetailsList = detailsList.map { item ->

            HrSlittingTransactionDetails(

                HRSlittingTranDtlId = item.hrSlittingTranDtlId,
                HRSlittingTranId = item.hrSlittingTranId,
                Width = item.width ?: 0.0,
                Barcode = item.barcode ?: "",
                    MaterialCode =  bomData.firstOrNull()?.MaterialCode ?: "",

                WeighAfterSlitting = item.weighAfterSlitting ?: 0.0,
                WeightTakenBy = userName ?: "",
                WeightLocationId = locationId,
                WeightDatetime = getCurrentDateTimeISO(),

                IsActive = true,
                Status = CompleteStatus,

                Component = bomData
                    .flatMap { it.Outputs }
                    .flatMap { it.Component }
            )
        }


        val request = HrSlittingRequest(

            HRSlittingTranId = tranPlanId,
            TenantCode = tenantCode ?: "",
            HRSlittingPlanId= hrSlittingPlanId,
            LocationId  = locationId,

            SourceStockId = sourceStockId,
            Weight=motherWeight,

            JobNumber = binding.textJobNumber.text
                .toString()
                .replace("Job #", ""),
            Barcode=barcode,
            IronLossWeight = ironLossWeight,
            ScrapWeight = scrapWeight,
            CompletedBy = "",
            CompletedDate = getCurrentDateTimeISO(),
            Status = CompleteStatus,
            Remarks = "Completed Slitting",
            IsPlanned= true,   // TODO: \
            Process=selectedProcess,
            MachineName=selectedMachineName,
            Tamper="",
            Grade =grade,
            hrSlittingTransactionDetail = apiDetailsList ?: emptyList()

        )


        slittingStatusViewModel.completeHrSlitting(request)
    }

    fun getSelectedBomData(): List<InputRequest> {

        return bomAdapter.getUpdatedData()
            .filter { it.inputWeight > 0 } // ✅ only selected inputs
            .map { input ->

                InputRequest(
                    MaterialCode = input.inputMaterial,
                    Weight = input.inputWeight,

                    Outputs = input.boMOutput
                        .filter { it.weight > 0 } // ✅ only selected outputs
                        .map { output ->

                            OutputRequest(
                                MaterialCode = output.outputMaterial,
                                Weight = output.weight,

                                Component = output.boMComponent
                                    .filter { it.weight > 0 } // ✅ only selected components
                                    .map { comp ->

                                        ComponentRequest(
                                            MaterialCode = comp.componentCode,
                                            Weight = comp.weight
                                        )
                                    }
                            )
                        }
                )
            }
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
                    ChildMotherExceedError
                ).show()

                isWeightErrorShown = true
            }

            binding.layoutScrapTable.tvIronLossValue.setText("0.00")

            return
        } else {
            // Reset flag when valid
            isWeightErrorShown = false
        }


        //  Calculate Iron Loss
        val calculatedIronLoss =
            motherWeight - (totalChildWeight + scrapWeightInput)

        val ironLoss =
            if (calculatedIronLoss < 0) 0.0 else calculatedIronLoss


        ironLossWeight = ironLoss
        scrapWeight = scrapWeightInput


        //  Show Iron Loss
        binding.layoutScrapTable.tvIronLossValue.setText(
            String.format("%.2f", ironLoss)
        )
    }
}


