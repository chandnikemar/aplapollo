package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.OutputDialogAdapter
import com.example.aplapollo.adapter.PicklingJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.Utils.showErrorDialog
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.model.Pickling.PicklingComponent
import com.example.aplapollo.model.Pickling.PicklingTransactionDetail
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
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

                // ======================
                // CALL BOM API
                // ======================

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
            adapter.addJob()
            binding.recyclerOutput.scrollToPosition(
                adapter.itemCount - 1
            )
            updateUI()
        }

        updateUI()


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
        binding.idLayoutHeader.tvTitle.text = selectedProcess+"PROCESS"
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
                        val mUom = response?.uoM
                        inputBarcode =
                            response.motherBarcode ?: ""

                        inputMaterialCode =
                            response.materialCode ?: ""

                        motherWeight =
                            response.motherCoilWeight ?: 0.0

                        binding.textInputMaterial.text =
                            inputMaterialCode

                        binding.textJobNumber.text =
                            response.jobNumber ?: ""

                        binding.tvMotherCoil.setText(
                            inputBarcode
                        )

                        binding.tvBatchNumber.setText(
                            String.format(
                                "%.2f %s",
                                motherWeight,
                                mUom.toString()
                            )
                        )
                        // =====================================
                        // RECYCLER LIST
                        // =====================================

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

                            showOutputDialog(
                                outputs
                            )
                        }
                    }

                    is Resource.Error -> {

                        progress.dismiss()
                        val errorMsg = res.message ?: "Failed to complete"

                        Log.e("API_ERROR", errorMsg)

                        Utils.showErrorDialog(this, errorMsg)
                        Toasty.error(
                            this,
                            res.message ?: "Error"
                        ).show()
                    }

                    else -> {}
                }
            }
        // =========================================
// ADD CHILD API
// =========================================

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
        }


    // =========================================
    // OUTPUT DIALOG
    // =========================================

    private fun showOutputDialog(
        outputs: List<BomOutput>
    ) {

        val dialog =
            AlertDialog.Builder(this)
                .create()

        val view =
            layoutInflater.inflate(
                R.layout.dialog_output_material,
                null
            )

        val recycler =
            view.findViewById<
                    RecyclerView>(
                R.id.recyclerOutput
            )

        recycler.layoutManager =
            LinearLayoutManager(this)

        recycler.adapter =
            OutputDialogAdapter(
                outputs
            ) { selected ->

                adapter.setSelectedOutput(
                    pendingPosition,
                    selected
                )

                dialog.dismiss()
            }

        dialog.setView(view)

        dialog.show()
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


        if (updatedList.isEmpty()) {
            showErrorDialog(this, "No data to submit")
            return
        }

        // ================= JOB VALIDATION =================

        for ((index, job) in updatedList.withIndex()) {

            val detail =
                job.picklingTransactionDetails
                    ?.firstOrNull()

            // Output material validation
            if (detail?.selectedOutputMaterial == null) {

                showErrorDialog(
                    this,
                    "Please select Output Material for Job ${index + 1}"
                )
                return
            }

            // Output weight validation
            val outputWeight =
                detail.weightAfterPickling ?: 0.0

            if (outputWeight <= 0.0) {

                showErrorDialog(
                    this,
                    "Enter output weight for Job ${index + 1}"
                )
                return
            }

            // Component validation
            if (detail.components.isNullOrEmpty()) {

                showErrorDialog(
                    this,
                    "Components are missing for Job ${index + 1}"
                )
                return
            }

            // Component weight validation
            val hasEmptyWeight =
                detail.components!!.any {
                    (it.weight ?: 0.0) <= 0.0
                }

            if (hasEmptyWeight) {

                showErrorDialog(
                    this,
                    "Enter component weight for Job ${index + 1}"
                )
                return
            }

            // OPTIONAL:
            // Total component weight should match output weight

            val totalComponentWeight =
                detail.components!!.sumOf {
                    it.weight ?: 0.0
                }
        }

        // ================= SCRAP CALCULATION =================

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

            jobNumber = binding.textJobNumber.text.toString(),

            status = "Completed",
            remarks = "Pickling is completed",

            isDivided = false,
            IsActive = true,

            inputBarcode = inputBarcode,
            inputWeight = motherWeight.toInt(),

            ironLossWeight = ironLossWeight.toInt(),

            scrapWeight = scrap,

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
    private fun updateUI() {

        if (adapter.itemCount == 0) {

            binding.recyclerOutput.visibility =
                View.GONE

        } else {

            binding.recyclerOutput.visibility =
                View.VISIBLE
        }
    }}