package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
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
        mutableListOf<
                PicklingTransactionResponse>()

    private var pendingPosition = -1

    private var transactionId = 0

    private var tenantCode = ""

    private var inputBarcode = ""

    private var inputMaterialCode = ""

    private var motherWeight = 0.0
    private var locationId: Int = 0
    private var sourceStockId: Int = 0
    private lateinit var selectedProcess: String
    private lateinit var selectedMachineName: String

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
        binding.idLayoutHeader.tvTitle.text = "Pickling Status"

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.recyclerOutput.isNestedScrollingEnabled = false
        init()

        setupRecycler()

        setupClicks()

        observeData()


        picklingViewModel
            .fetchPicklingTransactionById(
                transactionId
            )
        binding.btnSave.setOnClickListener {

            submitPickling()
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

    // =========================================
    // RECYCLER
    // =========================================

    private fun setupRecycler() {

        adapter =
            PicklingJobAdapter(

                context = this,

                jobList = jobList,

                onDelete = {

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

        binding.recyclerOutput
            .layoutManager =
            LinearLayoutManager(this)

        binding.recyclerOutput
            .adapter =
            adapter
        // IMPORTANT
        binding.recyclerOutput.isNestedScrollingEnabled = false
    }



    private fun setupClicks() {

        binding.btnAddJob.setOnClickListener {

            picklingViewModel.fetchPicklingAddChild(
                transactionId,
                tenantCode
            )
        }

        binding.btnSave.setOnClickListener {

            val updated =
                adapter.getUpdatedList()

            Log.d(
                "FINAL_SAVE",
                updated.toString()
            )
        }
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
                            motherWeight.toString()
                        )

                        // =====================================
                        // RECYCLER LIST
                        // =====================================

                        jobList.clear()

                        // if details available
                        if (!response.picklingTransactionDetails.isNullOrEmpty()) {

                            response.picklingTransactionDetails?.forEach { detail ->

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
                                        mutableListOf(detail)
                                    )

                                jobList.add(row)

                                Log.d(
                                    "DETAIL_BARCODE",
                                    "Barcode = ${detail.barcode}"
                                )
                            }

                        } else {

                            // fallback single row
                            jobList.add(response)
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

        // =====================================
        // BOM OUTPUT API
        // =====================================

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

        val updatedList =
            adapter.getUpdatedList()

        // =========================================
        // DETAILS
        // =========================================

        val detailList =
            mutableListOf<PicklingTransactionDetail>()

        updatedList.forEach { job ->

            job.picklingTransactionDetails?.forEach { detail ->

                // ================= COMPONENTS =================

                val componentList =
                    mutableListOf<PicklingComponent>()

                detail.components?.forEach { comp ->

                    componentList.add(

                        PicklingComponent(

materialCode=comp.componentCode,
                            weight =
                            comp.weight ?: 0.0,

                            uoM =
                            comp.Uom ?: "Kg"
                        )
                    )
                }

                // ================= DETAIL =================

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

                        uoM = "",

                        weightTakenBy = "",

                        weightDateTime =
                        Utils.getCurrentDateTimeISO(),

                        picklingComponent =
                        componentList
                    )
                )
            }
        }

        // =========================================
        // FINAL REQUEST
        // =========================================

        val request = ProcessPicklingRequest(

            picklingTranId = transactionId,

            tenantCode = tenantCode,

            locationId = locationId,

            sourceStockId = sourceStockId,

            jobNumber =
            binding.textJobNumber.text.toString(),

            status = "Completed",

            remarks = "Pickling Is completed",

            isDivided = false,

            IsActive = true,

            inputBarcode = inputBarcode,

            inputWeight = motherWeight.toInt(),

            ironLossWeight = 0,

            scrapWeight = 0,

            completedBy =
            session.getUserDetails()["userName"].toString(),

            completedDate =
            Utils.getCurrentDateTimeISO(),

            process = selectedProcess,

            machineName = selectedMachineName,

            tamper = "",

            grade =
            binding.etGrade.text.toString(),

            PicklingTransactionDetails =
            detailList
        )

        // =========================================
        // LOG
        // =========================================

        Log.d(
            "FINAL_REQUEST",
            request.toString()
        )

        // =========================================
        // API CALL
        // =========================================

        picklingViewModel.submitPickling(
            request
        )
    }
}