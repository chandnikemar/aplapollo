    package com.example.aplapollo.view.coldpressing
    
    import android.annotation.SuppressLint
    import android.app.ProgressDialog
    import android.os.Build
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.ArrayAdapter
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
    import com.example.aplapollo.helper.Utils.showSearchableOutputDialog
    import com.example.aplapollo.model.BomOutput
    import com.example.aplapollo.model.CRM.CRMTransactionRequest
    import com.example.aplapollo.model.CRM.CRMTransactionResponse
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
        private lateinit var crmAdapter: CRMAdapter
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
        private var withoutCrmWeight: Double = 0.0
        private var isCoilDivided: Boolean = false
        private lateinit var selectedProcess:String
        private lateinit var selectedMachineName:String
        private lateinit var grade:String
        private var isWeightErrorShown = false
        private var inputMaterialCode: String = ""
        private var inputBarcode:String=""
        private var bomOutputs: List<BomOutput> = emptyList()
        private var pendingPosition = -1
        private var pendingDeletePosition = -1
        private val jobList =
            mutableListOf(
    
                CRMTransactionResponse(
                    crmTranId =0,
                )
            )
        private var completeTittle:String=""
        private var completeTittleCRCA:String=""
        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_crmtransaction)
    
    
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
    
    //        binding.recyclerOutput.adapter = crmAdapter
            // Get Intent extras safely
            selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
            selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
            completeTittle=intent.getStringExtra("Completed_PAGECRFH")?:""
            completeTittleCRCA=intent.getStringExtra("Completed_PAGECRCA")?:""
            tranId=intent.getIntExtra("CRM_TRAN_JOB",0)
            inputBarcode=intent.getStringExtra(Constants.BarcodeValue)?:""
    //        transactionId = intent.getIntExtra(Constants.PicklingId, 0)
            sourceStockId = intent.getIntExtra(Constants.SourceStockId, 0)
            locationId = intent.getIntExtra(LocationId, 0)
            grade= intent.getStringExtra(Constants.GradeV)?:""
            binding.idLayoutHeader.tvTitle.text = "$selectedProcess PROCESS"
            supportActionBar?.hide()
    
            Log.d("LOCATION_DEBUG", "Received LocationId = $locationId")
            if (tranId == 0) {
                Toasty.error(this, "Invalid Transaction ID").show()
                finish()
                return
            }
                binding.btnSave.isEnabled = false
    
    
    
            binding.recyclerOutput.isNestedScrollingEnabled = false
            printlabelViewModel.getGrades()
            crmAdapter = CRMAdapter(
                this,
                jobList,
    
                onDelete = { position, item ->
    
                    val detailId =
                        item.crmTransactionDetails
                            ?.firstOrNull()
                            ?.crmTransactionDetailsId
                            ?: 0
    
    
    
                    if (detailId > 0) {
                        pendingDeletePosition = position
                        crmViewModel.fetchCRMDeleteChild(
                            detailId
                        )
    
                    } else {
    
                        jobList.removeAt(position)
    
                        crmAdapter.notifyItemRemoved(position)
    
                        crmAdapter.notifyItemRangeChanged(
                            position,
                            jobList.size
                        )
                    }
                },
    
                onOutputClick = { position ->
    
                    pendingPosition =
                        position
    

    
                    bomViewModel.getBom(
                        inputMaterialCode
                    )
                }
            )
            binding.recyclerOutput.layoutManager =
                LinearLayoutManager(this)
    
            binding.recyclerOutput.adapter =
                crmAdapter
    
            crmViewModel.getCRMPlanTranDetailById(tranId)
            binding.btnSave.setOnClickListener {
    
                submitCRM()
            }
            binding.btnAddJob.setOnClickListener{
                crmViewModel.fetchCrmAddChild(crmTransId = tranId,tenantCode=tenantCode.toString())
    //            crmAdapter.addJob()
    //            binding.recyclerOutput.scrollToPosition(
    //                crmAdapter  .itemCount - 1
    //            )
                updateUI()
            }
    
            updateUI()
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
    //                        crmAdapter?.updateList(listOf(transaction.barcode ?: ""))
                            bindTransactionData(transaction)
                            jobList.clear()
    
                            if (!transaction.crmTransactionDetails.isNullOrEmpty()) {
    
                                transaction.crmTransactionDetails?.forEach { detail ->
    
                                    val row = CRMTransactionResponse(
    
                                        crmTranId = transaction.crmTranId,
    
                                        crmPlanId = transaction.crmPlanId,
    
                                        tenantCode = transaction.tenantCode,
    
                                        sourceStockId = transaction.sourceStockId,
    
                                        locationId = transaction.locationId,
    
                                        jobNumber = transaction.jobNumber,
    
                                        barcode = detail.barcode,
    
                                        motherBarcode = transaction.motherBarcode,
    
                                        motherCoilWeight = transaction.motherCoilWeight,
    
                                        materialCode = transaction.materialCode,
    
                                        crmTransactionDetails = mutableListOf(detail)
                                    )
    
                                    jobList.add(row)
                                }
    
                            } else {
    
                                jobList.add(transaction)
                            }
    
                            crmAdapter.notifyDataSetChanged()
                            updateUI()
    
                            binding.btnSave.isEnabled = true
    
    
                        }
                        is Resource.Error -> {
                            dismissProgress()
                            Toasty.error(this, resource.message ?: "Error fetching transaction").show()
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
    
            bomViewModel
                .bomLiveData
                .observe(this) { res ->
    
                    when (res) {
    
                        is Resource.Loading -> {
    
                            progress.show()
                        }
    
                        is Resource.Success -> {
    
                            progress.dismiss()
    
                            val outputs =
                                res.data?.flatMap {
    
                                    it.boMOutput
    
                                } ?: emptyList()
    
                            if (
                                outputs.isNotEmpty()
                                &&
                                pendingPosition != -1
                            ) {
    
                                showSearchableOutputDialog(
                                    activity = this,
                                    title = "Select Output Material",
                                    items = outputs
                                ) { selected ->
    
                                    crmAdapter.setSelectedOutput(
                                        pendingPosition,
                                        selected
                                    )
                                    pendingPosition = -1
                                }
                            }
                        }
    
                        is Resource.Error -> {
    
                            progress.dismiss()
                            val errorMsg = res.message ?: "Failed to complete"
    
                            Log.e("API_ERROR", errorMsg)
    
                            Utils.showErrorDialog(this, errorMsg)
    
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
                            response?.responseMessage ?: " initiated",
                            Toasty.LENGTH_SHORT
                        ).show()
//                        val printRequestList = listOf(
//                            PrintLabelBarcodeRequest(
//                                barcode = barcode,
//                                locationId = locationId,
//                                createdDate = Utils.getCurrentDateTimeISO(),
//                                createdBy = userName ?: ""
//                            )
//                        )
//
//                        printlabelViewModel.printLabelBarcode(printRequestList)
    
                        finish()
                    }
    
                    is Resource.Error -> {
                        progress.dismiss()
                        resource.message?.let { Utils.showErrorDialog(this, it) }
    //                    Toasty.error(
    //                        this,
    //                        resource.message ?: "Failed to initiate CRM",
    //                        Toasty.LENGTH_SHORT
    //                    ).show()
                    }
    
                    else -> {}
                }
            }
            crmViewModel
                .crmAddChildLiveData
                .observe(this) { res ->
    
                    when (res) {
    
                        is Resource.Loading -> {
    
                            progress.show()
                        }
    
                        is Resource.Success -> {
    
                            progress.dismiss()
    
                            Toasty.success(
                                this,
                                "Child Added"
                            ).show()
    
    
    
                            crmViewModel
                                .getCRMPlanTranDetailById(
                                    tranId
                                )
                        }
    
                        is Resource.Error -> {
    
                            progress.dismiss()
    
                            Toasty.error(
                                this,
                                res.message ?: "Error"
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
    
            crmViewModel.crmDeleteChildLiveData.observe(this) { res ->
    
                when (res) {
    
                    is Resource.Loading -> {
                        progress.show()
                    }
    
                    is Resource.Success -> {
    
                        progress.dismiss()
    
                        Toasty.success(
                            this,
                            res.data?.responseMessage ?: "Deleted Successfully"
                        ).show()
    
                        // Reload updated child list
                        crmViewModel.getCRMPlanTranDetailById(tranId)
    
                        pendingDeletePosition = -1
                    }
    
                    is Resource.Error -> {
    
                        progress.dismiss()
    
                        pendingDeletePosition = -1
    
                        Utils.showErrorDialog(
                            this,
                            res.message ?: "Delete failed"
                        )
                    }
    
                    else -> {}
                }
            }
    
            binding.btnSave.setOnClickListener { submitCRM() }
    
        }
        private fun bindTransactionData(transaction: CRMTransactionResponse) {
            motherBarcode= transaction.motherBarcode.toString()
            binding.textJobNumber.text = "Job #${transaction.jobNumber}"
            binding.tvMotherCoil.setText(transaction.motherBarcode).toString()
            binding.tvBatchNumber.setText(transaction.motherCoilWeight.toString()+"Ton")
    //        binding.tvBarcode?.setText(transaction.barcode)
            barcode = transaction.barcode ?: ""
            binding.tvAllowance.text = "${transaction?.allowedToleranceWeightKg} Kg"
            binding.tvScrap.text = "${transaction?.allowedScrapWeightKg.toString()} Kg"
            binding.tvOutput.text = "${transaction?.allowedOutputWeightInTons.toString()} Ton"
            motherWeight = transaction.motherCoilWeight ?: 0.0
           withoutCrmWeight=transaction.weightAfterCRM?:0.0
            tranId = transaction.crmTranId!!
            planId = transaction.crmPlanId!!
            tenantCode = transaction.tenantCode
            sourceStockId = transaction.sourceStockId!!
            jobNumber = transaction.jobNumber.toString()
    //        binding.jobTable.editC4.isEnabled = true
    
            // ✅ ADD HERE
    //        crmAdapter?.updateList(listOf(barcode))
    
        }
    
    
    
    

    
        @RequiresApi(Build.VERSION_CODES.O)
        private fun submitCRM() {
            val gradeInput =
                binding.etGrade.text.toString().trim()

            if (gradeInput.isEmpty()) {
                Toasty.error(this, "Grade is required").show()
                return
            }
            val request = CRMTransactionRequest(
    
                crmTranId = tranId,
                crmPlanId = planId,
    //            tenantCode = tenantCode,
                locationId = locationId,
                sourceStockId = sourceStockId,
    
                desiredThickness = 0.0,
    
                weight =0.0,
    
                jobNumber = binding.textJobNumber.text
                    .toString()
                    .replace("Job #", ""),
    
                inputBarcode = motherBarcode,
    
                inputWeight = motherWeight,
    
    //            barcode = barcode,
    
                materialCode = inputMaterialCode,
    
                ironLossWeight = 0.0,
    
                scrapWeight =crmAdapter.getTotalComponentWeight(),
    
    //            weightAfterCRM =
    //            crmAdapter.getTotalOutputWeight(),
    
                isCoilDivided = false,
    
    //            dividedCRMTranId = null,
    
                completedBy = userName ?: "",
    
                completedDate =
                Utils.getCurrentDateTimeISO(),
    
                status = CompleteStatus,
    
                remarks = "CRM Transaction",
    
                isPlanned = false,
    
                process = selectedProcess,
    
                machineName = selectedMachineName,
    
                tamper = "",
    
                grade = gradeInput,
    
                crmTransactionDetails =
                userName?.let {
                    crmAdapter.getCRMTransactionDetails(
                        it,
                        barcode
                    )
                }
    
            )
    
            crmViewModel.processCRM(request)
        }
    
        private fun updateUI() {
    
            if (crmAdapter.itemCount == 0) {
    
                binding.recyclerOutput.visibility =
                    View.GONE
    
            } else {
    
                binding.recyclerOutput.visibility =
                    View.VISIBLE
            }
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