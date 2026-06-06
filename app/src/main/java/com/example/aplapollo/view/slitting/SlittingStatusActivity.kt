    package com.example.aplapollo.view.slitting


    import HrSlittingStatusTransactionDetail
    import android.annotation.SuppressLint
    import android.app.ProgressDialog
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.ArrayAdapter
    import androidx.annotation.RequiresApi
    import androidx.appcompat.app.AppCompatActivity
    import androidx.appcompat.app.AppCompatDelegate
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.ViewModelProvider
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.aplapollo.adapter.Slitting.SlittingStatusAdapter
    import com.example.aplapollo.api.RetrofitInstance
    import com.example.aplapollo.helper.Constants
    import com.example.aplapollo.helper.Constants.BarcodeValue
    import com.example.aplapollo.helper.Constants.CompleteStatus
    import com.example.aplapollo.helper.Constants.HrSlittingPlanId
    import com.example.aplapollo.helper.Constants.LocationId
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.helper.Utils
    import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
    import com.example.aplapollo.model.BomOutput
    import com.example.aplapollo.model.PrintLabelBarcodeRequest
    import com.example.aplapollo.model.Slitting.ComponentRequest
    import com.example.aplapollo.model.Slitting.HrSlittingCompleteRequest
    import com.example.aplapollo.model.Slitting.HrSlittingCompleteTransactionDetails
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
        private lateinit var binding:ActivitySlittingStatusBinding
        private lateinit var progress: ProgressDialog
        private lateinit var slittingStatusViewModel: SlittingStatusViewModel
        private lateinit var printlabelViewModel: PrintlabelViewModel
        private lateinit var bomViewModel: BomViewModel
        private lateinit var session: SessionManager
        private var slittingAdapter: SlittingStatusAdapter? = null


        private var baseUrl: String = ""
        private var userName: String? = ""
        private var token: String? = ""
        private var tenantCode: String? = ""
        private var userDetail: HashMap<String, Any?>? = null
        private var serverIpSharedPrefText: String? = null
        private var serverHttpPrefText: String? = null
        private var motherBarcode: String = ""
        private var barcode: String = ""
        private var supplierNo: String = ""
        private var tranPlanId: Int = 0
        private var locationId: Int = 0
        private var sourceStockId: Int = 0
        private var hrSlittingPlanId = 0

        private var motherWeight: Double = 0.00

        private var motherUom: String = ""
        private var scrapWeight: Double = 0.0
        private var ironLossWeight: Double = 0.0
        private var isWeightErrorShown = false
        private lateinit var selectedProcess: String
        private lateinit var selectedMachineName: String


        //    private lateinit var grade:String
        private var slittingList: MutableList<HrSlittingStatusTransactionDetail> = mutableListOf()
        private var bomOutputs: List<BomOutput> = emptyList()
        private var pendingDeletePosition = -1
        private var pendingPosition = -1
        private var inputMaterialCode: String = ""

        private var appConfigId: Int = 0
        private var isScrapEditable = false
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting_status)


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

            }
            supplierNo = barcode
            tranPlanId =
                intent.getIntExtra(
                    HrSlittingPlanId,
                    0
                )
//            sourceStockId = intent.getIntExtra(SourceStockId, 0)
            locationId = intent.getIntExtra(LocationId, 0)
    //        grade= intent.getStringExtra(GradeV)?:""
            hrSlittingPlanId = intent.getIntExtra(HrSlittingPlanId, 0)
            selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
            selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""

    //        val motherBarcode = intent.getStringExtra(BarcodeValue) ?: "--"
            motherBarcode = intent.getStringExtra(BarcodeValue) ?: ""
            Log.d("MOTHER_BARCODE", "MotherBarcode = $motherBarcode")
    //        val supplierNo = intent.getStringExtra("SupplierNo") ?: "--"


//            binding.textJobNumber.text = "Job #$jobId"




    //        binding.tvBatchNumber.setText("${motherWeight}"+"${u}")

            binding.idLayoutHeader.tvTitle.text = "$selectedProcess Process"

            var isExpanded = false

//            slittingStatusViewModel.getConfigByKey("SCRAP")


            printlabelViewModel.getGrades()
            binding.btnAddJob.setOnClickListener {
                slittingStatusViewModel.getSlittingAddChild(tranPlanId, tenantCode ?: "")
            }
            binding.recyclerOutput.isNestedScrollingEnabled = false
            slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)
            printlabelViewModel.getGrades()
            slittingStatusViewModel.hrSlittingDetailsLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Success -> {

                        val response = resource.data

                        slittingList = (response?.hRSlittingTransactionDetail ?: emptyList()).toMutableList()

                        // ✅ ADD LOGS HERE
                        Log.d("SLITTING_LIST", "Size = ${slittingList.size}")

                        slittingList.forEach {
                            Log.d("BARCODE", it.barcode ?: "NULL")
                        }

                        binding.recyclerOutput.isNestedScrollingEnabled = false
                        val inputBarcode=response?.motherBarcode
                        val materialCode = response?.materialCode ?: ""
                        val jobNumber= response?.jobNumber?:""
                         motherWeight= (response?.motherCoilWeight?:"") as Double
                        sourceStockId = (response?.sourceStockId?:"") as Int
                        motherUom = response?.uoM ?: "Tons"
                        val mUom = response?.uoM
                        inputMaterialCode = materialCode
                        val value = motherWeight.toDouble() ?: 0.0
                        binding.tvMotherCoil.setText(inputBarcode ?: "")
                        binding.textInputMaterial.setText(inputMaterialCode)
                        binding.textJobNumber.setText("Job#$jobNumber")
                        binding.tvBatchNumber.setText("%.3f Ton".format(value))

                        binding.tvAllowance.text = "${response?.allowedToleranceWeightKg} Kg"
                        binding.tvScrap.text = "${response?.allowedScrapWeightKg.toString()} Kg"
                        binding.tvOutput.text = "${response?.allowedOutputWeightInTons.toString()} Ton"
                        Log.d("CALL_BOM", "MaterialCode = $materialCode")

                        if (materialCode.isNotEmpty()) {
                            bomViewModel.getBom(materialCode)
                        }

                        setupAdapterIfReady()
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
                        val updatedList = adapter?.getUpdatedList()
//                        val printRequestList = updatedList?.map { item ->
//                            PrintLabelBarcodeRequest(
//                                barcode = item.barcode ?: "",
//                                locationId = locationId,
//                                createdDate = getCurrentDateTimeISO(),
//                                createdBy = userName.toString()
//                            )
//                        }
//
//                        if (printRequestList != null) {
//                            printlabelViewModel.printLabelBarcode(printRequestList)
//                        }
                        Toasty.success(
                            this,
                            " completed successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                        finish()                     }

                    is Resource.Error -> {
                        progress.dismiss()
                        val errorMsg = resource.message ?: "Failed to complete slitting"

                        Log.e("API_ERROR", errorMsg)

                        Utils.showErrorDialog(this, errorMsg)

                    }

                    else -> {}
                }
            }
            printlabelViewModel.gradeLiveData.observe(this) { res ->

                when (res) {

                    is Resource.Loading -> {

                        progress.show()
                    }

                    is Resource.Success -> {

                        progress.dismiss()

                        val gradeList =
                            res.data?.map {
                                it.grade ?: ""
                            } ?: emptyList()

                        val gradeAdapter = ArrayAdapter(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            gradeList
                        )

                        binding.etGrade.setAdapter(gradeAdapter)
                    }

                    is Resource.Error -> {

                        progress.dismiss()

                        Toasty.error(
                            this,
                            res.message ?: "Failed to load grades"
                        ).show()
                    }

                    else -> {}
                }
            }
            printlabelViewModel.barcodePrintLabelMutableLiveData.observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {

                        progress.dismiss()

                        Toasty.success(
                            this,
                            "Barcode printed successfully",
                            Toasty.LENGTH_SHORT
                        ).show()

                        Log.d("PRINT_SUCCESS", resource.data.toString())

                                       }

                    is Resource.Error -> {

                        progress.dismiss()

                        Toasty.error(
                            this,
                            resource.message ?: "Print failed",
                            Toasty.LENGTH_SHORT
                        ).show()

                        Log.e("PRINT_ERROR", resource.message ?: "")
                    }

                    else -> {}
                }
            }
            bomViewModel.bomLiveData.observe(this) { response ->

                when (response) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        val bomList = response.data ?: emptyList()
                        // ✅ flatten all outputs
                        bomOutputs = bomList.flatMap { it.boMOutput }


                        // ✅ LOG for debug
                        Log.d("BOM_OUTPUTS", "Size = ${bomOutputs.size}")
                        bomOutputs.forEach {
                            it.outputMaterial?.let { it1 -> Log.d("BOM_OUTPUT", it1) }
                        }

                        // ✅ now setup adapter
                        setupAdapterIfReady()


                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        Toasty.error(this, response.message ?: "Error").show()
                    }

                    else -> {}
                }
            }
            slittingStatusViewModel.addChildLiveData.observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {

                        progress.dismiss()

                        Toasty.success(
                            this,
                            "Row Added Successfully",
                            Toasty.LENGTH_SHORT
                        ).show()

                        // IMPORTANT
                        // Reload complete slitting details from server
                        slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)
                    }

                    is Resource.Error -> {

                        progress.dismiss()

                        Utils.showErrorDialog(
                            this,
                            resource.message ?: "Add failed"
                        )
                    }

                    else -> {}
                }
            }
            slittingStatusViewModel.deleteChildLiveData.observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> progress.show()

                    is Resource.Success -> {

                        progress.dismiss()

                        slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)

                        Toasty.success(this, "Deleted Successfully").show()
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        pendingDeletePosition = -1
                        Utils.showErrorDialog(this, resource.message ?: "Delete failed")
                    }

                    else -> {}
                }
            }
//            slittingStatusViewModel.configKeyLiveData.observe(this) { res ->
//
//                when (res) {
//
//                    is Resource.Success -> {
//
//                        val data = res.data
//                        val value = res.data?.value ?: "0"
//
//                        binding.etScrapPercent.setText("")
//                        binding.etScrapPercent.setText(value)
//                        binding.etScrapPercent.setSelection(value.length)
//
//                        appConfigId = data?.appConfigId ?: 0
//                        binding.etScrapPercent.apply {
//                            setText(value)
//                            setTextColor(Color.BLACK)
//                            isEnabled = false
//                            isFocusable = false
//                        }
////                        binding.etScrapPercent.setText(value)
//                    }
//
//                    is Resource.Error -> {
//                        Toast.makeText(this, res.message ?: "Error", Toast.LENGTH_SHORT).show()
//                    }
//
//                    else -> {}
//                }
//            }
//            slittingStatusViewModel.registerConfigLiveData.observe(this) { res ->
//
//                when (res) {
//
//                    is Resource.Success -> {
//
//                        Toast.makeText(this, "Saved Successfully", Toast.LENGTH_SHORT).show()
//
//
//                        binding.etScrapPercent.isEnabled = false
//                        binding.btnSaveScrap.visibility = View.GONE
//
//
//                        slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)
//                        slittingStatusViewModel.getConfigByKey("SCRAP")
//
//                        binding.btnSaveScrap.visibility = View.GONE
//                        binding.btnEditScrap.visibility = View.VISIBLE
//                    }
//
//                    is Resource.Error -> {
//                        Toast.makeText(this, res.message ?: "Save failed", Toast.LENGTH_SHORT).show()
//                    }
//
//                    else -> {}
//                }
//            }
//
//            binding.btnEditScrap.setOnClickListener {
//
//                binding.etScrapPercent.apply {
//                    isEnabled = true
//                    isFocusable = true
//                    isFocusableInTouchMode = true
//                    requestFocus()
//                    setSelection(text?.length ?: 0)
//                }
//
//                binding.btnEditScrap.visibility = View.GONE
//                binding.btnSaveScrap.visibility = View.VISIBLE
//            }
//            binding.btnSaveScrap.setOnClickListener {
//
//                val valueStr = binding.etScrapPercent.text.toString().trim()
//
//                if (valueStr.isEmpty()) {
//                    Toast.makeText(this, "Enter Scrap %", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//
//                val value = valueStr.toDoubleOrNull()
//
//                if (value == null) {
//                    Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//
//                // ✅ RANGE VALIDATION
//                if (value < 0 || value > 5) {
//                    Toast.makeText(
//                        this,
//                        "Scrap % must be between 0 and 5",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    return@setOnClickListener
//                }
//
//                val request = ApplicationConfigMaster(
//                    appConfigId = appConfigId,
//                    key = "SCRAP",
//                    value = value.toString()
//                )
//
//                slittingStatusViewModel.registerConfig(request)
//            }
//

            binding.btnPrintBarcode.setOnClickListener {

                val adapter = slittingAdapter
                val updatedList = adapter?.getUpdatedList() ?: emptyList()

                if (updatedList.isEmpty()) {
                    Toasty.error(
                        this,
                        "No barcode available for printing",
                        Toasty.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                val printRequestList = updatedList.map { item ->

                    PrintLabelBarcodeRequest(
                        barcode = item.barcode ?: "",
                        locationId = locationId,
                        createdDate = getCurrentDateTimeISO(),
                        createdBy = userName ?: ""
                    )
                }

                Log.d("PRINT_REQUEST", printRequestList.toString())


                printlabelViewModel.printLabelBarcode(printRequestList)
            }

            binding.btnSave.setOnClickListener {

                submitCompleteSlitting()

            }

        }
        private fun updateUI() {
            binding.recyclerOutput.visibility =
                if (slittingList.isEmpty()) View.GONE else View.VISIBLE
        }

        private fun setupAdapterIfReady() {

            Log.d(
                "SETUP_CHECK",
                "Slitting = ${slittingList.size}, BOM = ${bomOutputs.size}"
            )

            if (slittingList.isNotEmpty() && bomOutputs.isNotEmpty()) {

                slittingAdapter = SlittingStatusAdapter(
                    context = this,
                    list = slittingList,
                    getBomOutputs = { bomOutputs },

                    onAddClick = { position, item ->

                        val id = item.hrSlittingTranDtlId
                        val tenant = tenantCode ?: ""


                        slittingStatusViewModel.getSlittingAddChild(id,tenant)
                    },
                    onDeleteClick = { position, item ->

                        val id = item.hrSlittingTranDtlId

                        slittingStatusViewModel.getSlittingDeleteChild(id)
                    }
                )

                binding.recyclerOutput.layoutManager =
                    LinearLayoutManager(this)

                binding.recyclerOutput.adapter =
                    slittingAdapter

                Log.d(
                    "ADAPTER",
                    "Adapter initialized successfully"
                )
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun submitCompleteSlitting() {

            val adapter = slittingAdapter
            val detailsList = adapter?.getUpdatedList() ?: emptyList()

            val gradeInput = binding.etGrade.text.toString().trim()

            if (gradeInput.isEmpty()) {
                Toasty.error(this, "Grade is required").show()
                return
            }


            // =========================================================
            // WEIGHT CALCULATION ONLY
            // =========================================================
            val inputTon = motherWeight
            val totalOutputTon = adapter?.getTotalOutputWeight() ?: 0.0

            var totalComponentKg = 0.0
            val totalComponentWeightKg = detailsList.sumOf { item ->
                item.components?.sumOf { component ->
                    component.weight ?: 0.0
                } ?: 0.0
            }

            val totalComponentTon = totalComponentKg / 1000.0
            val totalConsumption = totalOutputTon + totalComponentTon

            Log.d(
                "WEIGHT_CHECK",
                "Input=$inputTon | Output=$totalOutputTon | Component=$totalComponentTon | Consumption=$totalConsumption"
            )
            detailsList.forEachIndexed { index, item ->

                Log.d(
                    "REQUEST_DEBUG",
                    "Row=$index " +
                            "Barcode=${item.barcode} " +
                            "Output=${item.selectedOutput?.outputMaterial} " +
                            "Weight=${item.weighAfterSlitting} " +
                            "Components=${item.components?.size ?: 0}"
                )
            }
            val apiDetailsList = detailsList.map { item ->
                val componentList = item.components?.map { component ->

                    ComponentRequest(
                        MaterialCode = component.componentCode ?: "",
                        MaterilDesc = component.materialDescription ?: "",
                        Weight = component.weight ?: 0.0,
                        Uom = "Kg"
                    )

                } ?: emptyList()
                HrSlittingCompleteTransactionDetails(
                    HRSlittingTranDtlId = item.hrSlittingTranDtlId,
                    HRSlittingTranId = item.hrSlittingTranId,
                    Width = item.width,
                    Barcode = item.barcode,
                    MaterialCode = item.selectedOutput?.outputMaterial ?: "",
                    WeighAfterSlitting = item.weighAfterSlitting ?: 0.0,
                    WeightTakenBy = userName ?: "",
                    WeightLocationId = locationId,
                    WeightDatetime = getCurrentDateTimeISO(),
                    IsActive = true,
                    Status = CompleteStatus,
                    Uom = "Tons",
                    Component = componentList
                )
            }

            val request = HrSlittingCompleteRequest(
                HRSlittingTranId = tranPlanId,
                TenantCode = tenantCode ?: "",
                HRSlittingPlanId = hrSlittingPlanId,
                LocationId = locationId,
                SourceStockId = sourceStockId,
                Weight = motherWeight,
                JobNumber = binding.textJobNumber.text.toString().replace("Job#", ""),
                Barcode = motherBarcode,
                IronLossWeight = 0.0,
                ScrapWeight = totalComponentWeightKg,
                CompletedBy = userName ?: "",
                CompletedDate = getCurrentDateTimeISO(),
                Status = CompleteStatus,
                Remarks = "Completed Slitting",
                IsPlanned = false,
                Process = selectedProcess,
                MachineName = selectedMachineName,
                Tamper = "",
                Grade = gradeInput,
                AllowedScrapWeightKg = 0.0,
                AllowedToleranceWeightKg = 0.0,
                hrSlittingTransactionDetail = apiDetailsList
            )

            slittingStatusViewModel.completeHrSlitting(request)

//            detailsList.forEach { item ->
//                val rowId = item.hrSlittingTranDtlId
//                val components = item.components?.map {
//                    ComponentRequest(
//                        MaterialCode = it.componentCode ?: "",
//                        MaterilDesc = it.materialDescription ?: "",
//                        Weight = it.weight ?: 0.0,
//                        Uom = "Kg"
//                    )
//                } ?: emptyList()
//
//
//                // =========================================================
//                // API DETAILS
//                // =========================================================
//                val apiDetailsList = detailsList.map { item ->
//                    val rowId = item.hrSlittingTranDtlId
//
//                    HrSlittingCompleteTransactionDetails(
//                        HRSlittingTranDtlId = rowId,
//                        HRSlittingTranId = item.hrSlittingTranId,
//                        Width = item.width,
//                        Barcode = item.barcode,
//                        MaterialCode = item.selectedOutput?.outputMaterial ?: "",
//                        WeighAfterSlitting = item.weighAfterSlitting ?: 0.0,
//                        WeightTakenBy = userName ?: "",
//                        WeightLocationId = locationId,
//                        WeightDatetime = getCurrentDateTimeISO(),
//                        IsActive = true,
//                        Status = CompleteStatus,
//                        Uom = "Tons",
//                        Component = components
//                    )
//                }
//
//
//                val request = HrSlittingCompleteRequest(
//                    HRSlittingTranId = tranPlanId,
//                    TenantCode = tenantCode ?: "",
//                    HRSlittingPlanId = hrSlittingPlanId,
//                    LocationId = locationId,
//                    SourceStockId = sourceStockId,
//                    Weight = inputTon,
//                    JobNumber = binding.textJobNumber.text.toString().replace("Job#", ""),
//                    Barcode = motherBarcode,
//                    IronLossWeight = 0.0,
//                    ScrapWeight = totalComponentWeightKg,
//                    CompletedBy = userName ?: "",
//                    CompletedDate = getCurrentDateTimeISO(),
//                    Status = CompleteStatus,
//                    Remarks = "Completed Slitting",
//                    IsPlanned = false,
//                    Process = selectedProcess,
//                    MachineName = selectedMachineName,
//                    Tamper = "",
//                    Grade = gradeInput,
//                    AllowedScrapWeightKg = 0.0,
//                    AllowedToleranceWeightKg = 0.0,
//                    hrSlittingTransactionDetail = apiDetailsList
//                )
//
//                Log.d("API_REQUEST", request.toString())
//
//
//                slittingStatusViewModel.completeHrSlitting(request)
//            }
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
        private fun refreshUI() {
            slittingAdapter?.notifyDataSetChanged()
            updateUI()
        }}