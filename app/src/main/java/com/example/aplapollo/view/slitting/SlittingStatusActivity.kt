    package com.example.aplapollo.view.slitting


    import HrSlittingStatusTransactionDetail
    import android.annotation.SuppressLint
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
    import com.example.aplapollo.helper.Constants.BarcodeValue
    import com.example.aplapollo.helper.Constants.CompleteStatus
    import com.example.aplapollo.helper.Constants.HrSlittingId
    import com.example.aplapollo.helper.Constants.HrSlittingPlanId
    import com.example.aplapollo.helper.Constants.JobId
    import com.example.aplapollo.helper.Constants.LocationId
    import com.example.aplapollo.helper.Constants.MotherWeightV
    import com.example.aplapollo.helper.Constants.SourceStockId
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.helper.Utils
    import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
    import com.example.aplapollo.model.BomOutput
    import com.example.aplapollo.model.PrintLabelBarcodeRequest
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
        private lateinit var binding: ActivitySlittingStatusBinding
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
        private var slittingList: List<HrSlittingStatusTransactionDetail> = emptyList()
        private var bomOutputs: List<BomOutput> = emptyList()

        private var inputMaterialCode: String = ""

        @SuppressLint("SetTextI18n")
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

            }

            binding.recyclerSlitting.layoutManager = LinearLayoutManager(this)

            supplierNo = barcode

            tranPlanId = intent.getIntExtra(HrSlittingId, 0)
            sourceStockId = intent.getIntExtra(SourceStockId, 0)
            locationId = intent.getIntExtra(LocationId, 0)
    //        grade= intent.getStringExtra(GradeV)?:""
            hrSlittingPlanId = intent.getIntExtra(HrSlittingPlanId, 0)
            selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
            selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
            val jobId = intent.getStringExtra(JobId) ?: "--"
    //        val motherBarcode = intent.getStringExtra(BarcodeValue) ?: "--"
            motherBarcode = intent.getStringExtra(BarcodeValue) ?: ""
            Log.d("MOTHER_BARCODE", "MotherBarcode = $motherBarcode")
    //        val supplierNo = intent.getStringExtra("SupplierNo") ?: "--"
            motherWeight = intent.getStringExtra(MotherWeightV)
                ?.toDoubleOrNull() ?: 0.0


//            binding.textJobNumber.text = "Job #$jobId"




    //        binding.tvBatchNumber.setText("${motherWeight}"+"${u}")


            slittingStatusViewModel.getHrSlittingDetailsById(tranPlanId)
            var isExpanded = false




            slittingStatusViewModel.hrSlittingDetailsLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Success -> {

                        val response = resource.data

                        slittingList = response?.hRSlittingTransactionDetail ?: emptyList()

                        // ✅ ADD LOGS HERE
                        Log.d("SLITTING_LIST", "Size = ${slittingList.size}")

                        slittingList.forEach {
                            Log.d("BARCODE", it.barcode ?: "NULL")
                        }

                        binding.recyclerSlitting.layoutManager = LinearLayoutManager(this)
val inputBarcode=response?.motherBarcode
                        val materialCode = response?.materialCode ?: ""
                        motherUom = response?.uoM ?: "Kg"
                        val mUom = response?.uoM
                        inputMaterialCode = materialCode
                        binding.tvMotherCoil.setText(inputBarcode ?: "")
                        binding.textInputMaterial.setText(inputMaterialCode ?: "")
                        binding.textJobNumber.setText(jobId ?: "")
                        binding.tvBatchNumber.setText(
                            String.format(
                                "%.2f %s",
                                motherWeight,
                                mUom.toString()
                            )
                        )

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
                        val updatedList = adapter?.getUpdatedTransactionDetails()
                        val printRequestList = updatedList?.map { item ->
                            PrintLabelBarcodeRequest(
                                barcode = item.barcode ?: "",
                                locationId = locationId,
                                createdDate = getCurrentDateTimeISO(),
                                createdBy = userName.toString()
                            )
                        }

                        if (printRequestList != null) {
                            printlabelViewModel.printLabelBarcode(printRequestList)
                        }
                        Toasty.success(
                            this,
                            "HR Slitting completed successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                        finish() // or navigate back
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        val errorMsg = resource.message ?: "Failed to complete slitting"

                        Log.e("API_ERROR", errorMsg)

                        Utils.showErrorDialog(this, errorMsg)
//                        Toasty.error(
//                            this,
//                            resource.message ?: "Failed to complete slitting",
//                            Toasty.LENGTH_LONG
//                        ).show()
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

                        finish()
                    }

                    is Resource.Error -> {
                        progress.dismiss()

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
                }
            }


            binding.btnPrintBarcode.setOnClickListener {


            }

            binding.btnSave.setOnClickListener {

                submitCompleteSlitting()

            }

        }

        private fun setupAdapterIfReady() {

            Log.d("SETUP_CHECK", "Slitting = ${slittingList.size}, BOM = ${bomOutputs.size}")

            if (slittingList.isNotEmpty() && bomOutputs.isNotEmpty()) {
                slittingAdapter = SlittingStatusAdapter(
                    context = this,
                    list = slittingList,
                    getBomOutputs = { bomOutputs },
                    inputWeightTon = motherWeight,
                    onWeightChanged = {
                    }
                )

                binding.recyclerSlitting.layoutManager = LinearLayoutManager(this)
                binding.recyclerSlitting.adapter = slittingAdapter

                Log.d("ADAPTER", "Adapter initialized successfully")
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        private fun submitCompleteSlitting() {

            val adapter = slittingAdapter
            val detailsList = adapter?.getUpdatedTransactionDetails() ?: emptyList()

            val gradeInput = binding.etGrade.text.toString().trim()

            // =========================================================
            // WEIGHT CALCULATION ONLY
            // =========================================================
            val inputTon = motherWeight
            val totalOutputTon = adapter?.getTotalOutputWeight() ?: 0.0

            var totalComponentKg = 0.0

            detailsList.forEach { item ->
                val rowId = item.hrSlittingTranDtlId
                val components = adapter?.getComponentsForRow(rowId) ?: emptyList()

                totalComponentKg += components.sumOf { it.Weight }
            }

            val totalComponentTon = totalComponentKg / 1000.0
            val totalConsumption = totalOutputTon + totalComponentTon

            Log.d(
                "WEIGHT_CHECK",
                "Input=$inputTon | Output=$totalOutputTon | Component=$totalComponentTon | Consumption=$totalConsumption"
            )

            // =========================================================
            // API DETAILS
            // =========================================================
            val apiDetailsList = detailsList.map { item ->
                val rowId = item.hrSlittingTranDtlId

                HrSlittingCompleteTransactionDetails(
                    HRSlittingTranDtlId = rowId,
                    HRSlittingTranId = item.hrSlittingTranId,
                    Width = item.width,
                    Barcode = item.barcode,
                    MaterialCode = adapter?.getSelectedOutputMaterial(rowId) ?: "",
                    WeighAfterSlitting = item.weighAfterSlitting ?: 0.0,
                    WeightTakenBy = userName ?: "",
                    WeightLocationId = locationId,
                    WeightDatetime = getCurrentDateTimeISO(),
                    IsActive = true,
                    Status = CompleteStatus,
                    Uom = adapter?.getSelectedUom(rowId) ?: "Kg",
                    Component = adapter?.getComponentsForRow(rowId) ?: emptyList()
                )
            }

            // =========================================================
            // REQUEST
            // =========================================================
            val request = HrSlittingCompleteRequest(
                HRSlittingTranId = tranPlanId,
                TenantCode = tenantCode ?: "",
                HRSlittingPlanId = hrSlittingPlanId,
                LocationId = locationId,
                SourceStockId = sourceStockId,
                Weight = inputTon,
                JobNumber = binding.textJobNumber.text.toString().replace("Job #", ""),
                Barcode = motherBarcode,
                IronLossWeight = 0.0,
                ScrapWeight = totalComponentKg,
                CompletedBy = userName ?: "",
                CompletedDate = getCurrentDateTimeISO(),
                Status = CompleteStatus,
                Remarks = "Completed Slitting",
                IsPlanned = true,
                Process = selectedProcess,
                MachineName = selectedMachineName,
                Tamper = "",
                Grade = gradeInput,
                hrSlittingTransactionDetail = apiDetailsList
            )

            Log.d("API_REQUEST", request.toString())

            // =========================================================
            // API CALL
            // =========================================================
            slittingStatusViewModel.completeHrSlitting(request)
        }}