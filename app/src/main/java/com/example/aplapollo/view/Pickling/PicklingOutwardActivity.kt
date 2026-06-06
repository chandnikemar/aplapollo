package com.example.aplapollo.view.Pickling

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
import com.example.aplapollo.adapter.PicklingJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.Utils.showErrorDialog
import com.example.aplapollo.helper.Utils.showSearchableOutputDialog
import com.example.aplapollo.model.Pickling.PicklingComponent
import com.example.aplapollo.model.Pickling.PicklingTransactionDetail
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingOutwardBinding
import es.dmoral.toasty.Toasty

class PicklingOutwardActivity :
    AppCompatActivity() {

    private lateinit var binding:
            ActivityPicklingOutwardBinding

    private lateinit var adapter:
            PicklingJobAdapter

    private lateinit var picklingViewModel:
            PicklingViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private lateinit var bomViewModel:
            BomViewModel

    private lateinit var session:
            SessionManager

    private lateinit var progress:
            ProgressDialog

    private val jobList =
        mutableListOf(

            PicklingTransactionResponse(
                picklingTranId = 0
            )
        )


    private var pendingPosition = -1

    private var transactionId = 0

    private var tenantCode = ""

    private var inputBarcode = ""

    private var inputMaterialCode = ""

    private var motherWeight = 0.0
    private var locationId: Int = 0
    private var sourceStockId: Int = 0
//    private  var picklingTransactionDetailId:Int=0
    private lateinit var selectedProcess: String
    private lateinit var selectedMachineName: String
    private var pendingDeletePosition = -1
    private  var jobId:String=""
    // =========================================
    // ON CREATE
    // =========================================

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(
                this,
                R.layout.activity_pickling_outward
            )


        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.recyclerOutput.isNestedScrollingEnabled = false

//        setContentView(binding.root)
        adapter = PicklingJobAdapter(
            this,
            jobList,

            onDelete = { position, item ->

                val detailId =
                    item.picklingTransactionDetails
                        ?.firstOrNull()
                        ?.picklingTransactionDetailsId
                        ?: 0



                if (detailId > 0) {
                    pendingDeletePosition = position
                    picklingViewModel.fetchPicklingDeleteChild(
                        detailId
                    )

                } else {

                    jobList.removeAt(position)

                    adapter.notifyItemRemoved(position)

                    adapter.notifyItemRangeChanged(
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
            adapter

        init()
        printlabelViewModel.getGrades()

//        setupRecycler()



        observeData()


        picklingViewModel
            .fetchPicklingTransactionById(
                transactionId
            )
        binding.btnSave.setOnClickListener {

            submitPickling()
        }
        binding.btnAddJob.setOnClickListener{
            picklingViewModel.fetchPicklingAddChild(
                transactionId,
                tenantCode
            )
//            adapter.addJob()
//            binding.recyclerOutput.scrollToPosition(
//                adapter.itemCount - 1
//            )
            updateUI()
        }

        updateUI()
        binding.btnPrintBarcode.setOnClickListener {

            val updatedList =
                adapter.getUpdatedList()

            if (updatedList.isEmpty()) {

                Toasty.error(
                    this,
                    "No barcode available for printing",
                    Toasty.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val printRequestList =
                updatedList.mapNotNull { item ->

                    val detail =
                        item.picklingTransactionDetails
                            ?.firstOrNull()

                    val barcode =
                        detail?.barcode ?: return@mapNotNull null

                    PrintLabelBarcodeRequest(

                        barcode = barcode,

                        locationId = locationId,

                        createdDate =
                        Utils.getCurrentDateTimeISO(),

                        createdBy =
                        session.getUserDetails()["userName"]
                            .toString()
                    )
                }

            if (printRequestList.isEmpty()) {

                Toasty.error(
                    this,
                    "No valid barcode found",
                    Toasty.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            Log.d(
                "PRINT_REQUEST",
                printRequestList.toString()
            )

            // API CALL
            printlabelViewModel
                .printLabelBarcode(printRequestList)
        }

    }


    private fun init() {

        progress =
            ProgressDialog(this).apply {

                setCancelable(false)

                setMessage("Please wait...")
            }

        session = SessionManager(this)

        val user =
            session.getUserDetails()

        tenantCode =
            user[
                SessionManager
                    .Key_tenantCode
            ].toString()

        transactionId =
            intent.getIntExtra(
                Constants.PicklingId,
                0
            )
        sourceStockId = intent.getIntExtra(Constants.SourceStockId, 0)
        locationId = intent.getIntExtra(Constants.LocationId, 0)
        selectedProcess = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
        binding.idLayoutHeader.tvTitle.text = "$selectedProcess Process"
        val retrofit =
            RetrofitInstance
                .getInstance(
                    applicationContext
                )

        picklingViewModel =
            ViewModelProvider(
                this,
                PicklingViewModelfactory(
                    application,
                    retrofit
                )
            )[PicklingViewModel::class.java]

        bomViewModel =
            ViewModelProvider(
                this,
                BomInputCodeViewModelfactory(
                    application,
                    retrofit
                )
            )[BomViewModel::class.java]

        printlabelViewModel =
            ViewModelProvider(this, QcprintlabelViewModelFactory(application, retrofit))[PrintlabelViewModel::class.java]
    }



    private fun observeData() {



        picklingViewModel
            .picklingTransactionLiveData
            .observe(this) { res ->

                when (res) {

                    is Resource.Loading -> {

                        progress.show()
                    }

                    is Resource.Success -> {

                        progress.dismiss()

                        Log.d(
                            "PICKLING_API",
                            "Response = ${res.data}"
                        )

                        val response =
                            res.data ?: return@observe

                        // =====================================
                        // TOP HEADER DATA
                        // =====================================

                        locationId = response.locationId ?: 0
                        sourceStockId = response.sourceStockId ?: 0
                        jobId=response.jobNumber?:""
                        val mUom = response?.uoM
                        inputBarcode =
                            response.motherBarcode ?: ""

                        inputMaterialCode =
                            response.materialCode ?: ""

                        motherWeight =
                            response.motherCoilWeight ?: 0.0

                        binding.textInputMaterial.text =
                            inputMaterialCode

                     binding.textJobNumber.text ="Job#$jobId"

                        binding.tvMotherCoil.setText(
                            inputBarcode
                        )

                        binding.tvBatchNumber.setText(
                            String.format(
                                "%.3f %s",
                                motherWeight,
                                 "Tons"
                            )
                        )


                        jobList.clear()

                        // if details available
                        if (!response.picklingTransactionDetails.isNullOrEmpty()) {

                            response.picklingTransactionDetails?.forEach { detail ->
//picklingTransactionDetailId= response.picklingTransactionDetails!![0].picklingTransactionDetailsId!!
                                val row =
                                    PicklingTransactionResponse(

                                        picklingTranId =
                                        response.picklingTranId,

                                        jobNumber =
                                        response.jobNumber,

                                        motherBarcode =
                                        detail.barcode, // IMPORTANT

                                        materialCode =
                                        response.materialCode,

                                        motherCoilWeight =
                                        response.motherCoilWeight,

                                        picklingTransactionDetails =
                                        mutableListOf(detail),

                                    )

                                jobList.add(row)

                                Log.d(
                                    "DETAIL_BARCODE",
                                    "Barcode = ${detail.barcode}"
                                )
                            }

                        } else {
                            jobList.add(
                                response.copy(
                                    isDefaultRow = true
                                ))
//                            // fallback single row
//                            jobList.add(response)
                        }

                        adapter.notifyDataSetChanged()
                    }

                    is Resource.Error -> {

                        progress.dismiss()
                        val errorMsg = res.message ?: " ERROR"
                        Log.e(
                            "PICKLING_API",
                            "Error = ${res.message}"
                        )
                        Utils.showErrorDialog(this, errorMsg)
//                        Toasty.error(
//                            this,
//                            res.message ?: "Error"
//                        ).show()
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
                                items = outputs
                            )
                            { selected ->

                                adapter.setSelectedOutput(
                                    pendingPosition,
                                    selected
                                )
                            }
                        }
                    }

                    is Resource.Error -> {

                        progress.dismiss()
                        val errorMsg = res.message ?: "Failed to complete"

                        Log.e("API_ERROR", errorMsg)

                        showErrorDialog(this, errorMsg)

                    }

                    else -> {}
                }
            }
        picklingViewModel
            .picklingAddChildLiveData
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

                        // =========================
                        // RELOAD PICKLING DATA
                        // =========================

                        picklingViewModel
                            .fetchPicklingTransactionById(
                                transactionId
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
        picklingViewModel.processPicklingLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()

                    Toasty.success(
                        this,
                        res.data ?: "Saved Successfully"
                    ).show()

                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()

                    val errorMsg = res.message ?: "Submission failed"

                    Log.e("SUBMIT_ERROR", errorMsg)

                    Utils.showErrorDialog(this, errorMsg)
                }

                else -> {}
            }
        }
        picklingViewModel.picklingDeleteChildLiveData.observe(this) {

            when (it) {

                is Resource.Success -> {

                    Toasty.success(
                        this,
                        it.data?.responseMessage
                            ?: "Deleted"
                    ).show()

                    // remove locally after success

                    val position =
                        pendingDeletePosition

                    // ✅ ADD HERE
                    if (
                        position != -1 &&
                        position < jobList.size
                    ) {

                        jobList.removeAt(position)

                        adapter.notifyItemRemoved(position)

                        adapter.notifyItemRangeChanged(
                            position,
                            jobList.size
                        )
                    }

                    // ✅ RESET HERE
                    pendingDeletePosition = -1
                }

                is Resource.Error -> {

                    pendingDeletePosition = -1

                    Toasty.error(
                        this,
                        it.message ?: "Delete failed"
                    ).show()
                }

                else -> {}
            }
        }

//            printlabelViewModel.barcodePrintLabelMutableLiveData.observe(this) { resource ->
//
//                when (resource) {
//
//                    is Resource.Loading -> {
//                        progress.show()
//                    }
//
//                    is Resource.Success -> {
//
//                        progress.dismiss()
//
//                        Toasty.success(
//                            this,
//                            "Barcode printed successfully",
//                            Toasty.LENGTH_SHORT
//                        ).show()
//
//                        Log.d("PRINT_SUCCESS", resource.data.toString())
//
//                        // OPTIONAL:
//                        // Call physical printer here
//
//                        // finish()
//                    }
//
//                    is Resource.Error -> {
//
//                        progress.dismiss()
//
//                        Toasty.error(
//                            this,
//                            resource.message ?: "Print failed",
//                            Toasty.LENGTH_SHORT
//                        ).show()
//
//                        Log.e("PRINT_ERROR", resource.message ?: "")
//                    }
//
//                    else -> {}
//                }
//            }
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
        }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitPickling() {

        val updatedList = adapter.getUpdatedList()
        val gradeInput = binding.etGrade.text.toString().trim()

        val ironLossWeight =
            binding.etIronLossWeight.text
                .toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0

        // ================= VALIDATION =================

        if (gradeInput.isEmpty()) {
            showErrorDialog(this, "Grade is required")
            return
        }
        if (ironLossWeight.toString().isEmpty()) {
            showErrorDialog(this, "IronLoss Weight is required")
            return
        }


        if (updatedList.isEmpty()) {
            showErrorDialog(this, "No data to submit")
            return
        }


        for ((index, job) in updatedList.withIndex()) {

            val detail =
                job.picklingTransactionDetails
                    ?.firstOrNull()



            // Component weight validation
            val hasEmptyWeight =
                detail?.components!!.any {
                    (it.weight ?: 0.0) <= 0.0
                }


            val totalComponentWeight =
                detail?.components!!.sumOf {
                    it.weight ?: 0.0
                }


            val scrap =
                motherWeight -
                        updatedList.sumOf {
                            it.picklingTransactionDetails
                                ?.firstOrNull()
                                ?.weightAfterPickling
                                ?: 0.0
                        }

            // ================= REQUEST BUILD =================

            val detailList = mutableListOf<PicklingTransactionDetail>()

            updatedList.forEach { job ->

                job.picklingTransactionDetails?.forEach { detail ->

                    val componentList =
                        mutableListOf<PicklingComponent>()

                    detail.components?.forEach { comp ->

                        componentList.add(
                            PicklingComponent(
                                materialCode = comp.componentCode,
                                weight = comp.weight ?: 0.0,
                                uoM = comp.Uom ?: "Kg"
                            )
                        )
                    }

                    detailList.add(
                        PicklingTransactionDetail(
                            picklingTransactionDetailsId =
                            detail.picklingTransactionDetailsId ?: 0,

                            barcode =
                            detail.barcode ?: "",

                            materialCode =
                            detail.selectedOutputMaterial?.outputMaterial ?: "",

                            width = 0,

                            weightAfterPickling =
                            detail.weightAfterPickling ?: 0.0,

                            uoM = "Tons",

                            weightTakenBy = "",

                            weightDateTime =
                            Utils.getCurrentDateTimeISO(),

                            picklingComponent =
                            componentList
                        )
                    )
                }
            }

            // ================= FINAL REQUEST =================

            val request = ProcessPicklingRequest(

                picklingTranId = transactionId,
                tenantCode = tenantCode,
                locationId = locationId,
                sourceStockId = sourceStockId,
                jobNumber = jobId,
                status = "Completed",
                remarks = "Pickling is completed",
                isDivided = false,
                IsActive = true,
                inputBarcode = inputBarcode,
                inputWeight = motherWeight,
                ironLossWeight = ironLossWeight,
                scrapWeight = totalComponentWeight,

                completedBy =
                session.getUserDetails()["userName"].toString(),

                completedDate =
                Utils.getCurrentDateTimeISO(),

                process = selectedProcess,
                machineName = selectedMachineName,
                tamper = "",

                grade = gradeInput,

                PicklingTransactionDetails = detailList
            )

            Log.d("FINAL_REQUEST", request.toString())

            // ================= API CALL =================

            picklingViewModel.submitPickling(request)
        }
    }
    private fun updateUI() {

        if (adapter.itemCount == 0) {

            binding.recyclerOutput.visibility =
                View.GONE

        } else {

            binding.recyclerOutput.visibility =
                View.VISIBLE
        }
    }}