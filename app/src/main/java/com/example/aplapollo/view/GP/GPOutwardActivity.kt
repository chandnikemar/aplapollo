package com.example.aplapollo.view.GP

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
import com.example.aplapollo.adapter.Gp.GpJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.GP.GalvanizingComponentRequest
import com.example.aplapollo.model.GP.GalvanizingTransactionDetailRequest
import com.example.aplapollo.model.GP.GalvanizingTransactionRequest
import com.example.aplapollo.model.GP.GalvanizingTransactionResponse
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.GP.GpViewModel
import com.example.aplapollo.viewmodel.GP.GpViewModelFactory
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGpoutwardBinding
import es.dmoral.toasty.Toasty

class GPOutwardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGpoutwardBinding

    private lateinit var adapterGp: GpJobAdapter

    private lateinit var gpViewModel: GpViewModel

    private lateinit var bomViewModel: BomViewModel

    private lateinit var printlabelViewModel: PrintlabelViewModel

    private lateinit var session: SessionManager

    private lateinit var progress: ProgressDialog

    private val jobList = mutableListOf<GalvanizingTransactionResponse>()

    private var pendingPosition = -1

    private var transactionId = 0

    private var tenantCode = ""

    private var inputBarcode = ""

    private var inputMaterialCode = ""

    private var motherWeight = 0.0

    private var zincWeight = 0.0

    private var motherzincWeight = 0.0

    private var zincMaterailCode = ""

    private var locationId: Int = 0

    private var sourceStockId: Int = 0

    private lateinit var selectedProcess: String

    private lateinit var selectedMachineName: String

    private var pendingDeletePosition = -1

    private var jobId: String = ""



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_gpoutward
        )

        init()

        setupRecycler()

        observeData()

        gpViewModel.fetchGpTransactionById(transactionId)

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            submitGP()
        }

        binding.btnAddJob.setOnClickListener {

            gpViewModel.fetchGpAddChild(
                transactionId,
                tenantCode
            )
        }
        val temperList = listOf("Soft", "Hard", "MixHard")

        val temperAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            temperList
        )

        binding.etTemper.setAdapter(temperAdapter)
        printlabelViewModel.getGrades()
        printlabelViewModel.getGSM()
        binding.btnPrintBarcode.setOnClickListener {

            val updatedList =
                adapterGp.getUpdatedList()



            if (updatedList.isEmpty()) {

                Toasty.error(
                    this,
                    "No data available for printing",
                    Toasty.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }



            val printRequestList =
                mutableListOf<PrintLabelBarcodeRequest>()

            updatedList.forEach { item ->

                item.galvanizingTransactionDetails?.forEach { detail ->

                    val barcode =
                        detail.barcode?.trim()

                    // CHECK BARCODE EMPTY
                    if (barcode.isNullOrEmpty()) {

                        Toasty.error(
                            this,
                            "Barcode missing",
                            Toasty.LENGTH_SHORT
                        ).show()

                        return@forEach
                    }

                    printRequestList.add(

                        PrintLabelBarcodeRequest(

                            barcode = barcode,

                            locationId = locationId,

                            createdDate =
                            Utils.getCurrentDateTimeISO(),

                            createdBy =
                            session.getUserDetails()["userName"]
                                .toString()
                        )
                    )
                }
            }


          if (printRequestList.isEmpty()) {

                Toasty.error(
                    this,
                    "No barcode found for printing",
                    Toasty.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            Log.d(
                "PRINT_REQUEST",
                printRequestList.toString()
            )


            printlabelViewModel
                .printLabelBarcode(printRequestList)
        }


    }


    private fun init() {

        progress = ProgressDialog(this).apply {
            setCancelable(false)
            setMessage("Please wait...")
        }

        session = SessionManager(this)

        val user = session.getUserDetails()

        tenantCode =
            user[SessionManager.Key_tenantCode].toString()

        transactionId =
            intent.getIntExtra("GPTranId", 0)

        sourceStockId =
            intent.getIntExtra(Constants.SourceStockId, 0)

        locationId =
            intent.getIntExtra(Constants.LocationId, 0)

        selectedProcess =
            intent.getStringExtra("PROCESS_NAME") ?: ""

        selectedMachineName =
            intent.getStringExtra("MACHINE_NAME") ?: ""

        binding.idLayoutHeader.tvTitle.text =
            "$selectedProcess Process"

        val retrofit =
            RetrofitInstance.getInstance(applicationContext)

        gpViewModel =
            ViewModelProvider(
                this,
                GpViewModelFactory(application, retrofit)
            )[GpViewModel::class.java]

        bomViewModel =
            ViewModelProvider(
                this,
                BomInputCodeViewModelfactory(application, retrofit)
            )[BomViewModel::class.java]

        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofit)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]
    }

    private fun setupRecycler() {

        binding.recyclerOutput.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerOutput.isNestedScrollingEnabled = false

        adapterGp = GpJobAdapter(

            context = this,

            jobList = jobList,

            onDelete = { position, item ->

                val detailId =
                    item.galvanizingTransactionDetails
                        ?.firstOrNull()
                        ?.galvanizingTransactionDetailsId
                        ?: 0

                if (detailId > 0) {

                    pendingDeletePosition = position

                    gpViewModel.fetchGpDeleteChild(detailId)

                } else {

                    if (
                        position >= 0 &&
                        position < jobList.size
                    ) {

                        jobList.removeAt(position)

                        adapterGp.notifyItemRemoved(position)

                        adapterGp.notifyItemRangeChanged(
                            position,
                            jobList.size
                        )

                        updateUI()
                    }
                }
            },

            onOutputClick = { position ->

                pendingPosition = position

                bomViewModel.getBom(inputMaterialCode)
            },

            inputMaterialcode = inputMaterialCode
        )

        binding.recyclerOutput.adapter = adapterGp

        binding.recyclerOutput.adapter = adapterGp
    }


    private fun observeData() {
        gpViewModel.gpDetailByIdLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> {

                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    val response = res.data ?: return@observe

                    transactionId =
                        response.galvanizingTranId ?: 0

                    locationId =
                        response.locationId ?: 0

                    sourceStockId =
                        response.sourceStockId ?: 0

                    jobId =
                        response.jobNumber ?: ""

                    inputBarcode =
                        response.motherBarcode ?: ""

                    inputMaterialCode =
                        response.materialCode ?: ""

                    motherWeight =
                        response.motherCoilWeight ?: 0.0

                    zincWeight =
                        response.zincWeight ?: 0.0

                    motherzincWeight =
                        response.motherCoilWeightWithZinc ?: 0.0

                    zincMaterailCode =
                        response.zincMaterialCode ?: ""

                    // =====================================================
                    // UI
                    // =====================================================

                    binding.textInputMaterial.text =
                        "$inputMaterialCode / $zincMaterailCode"

                    binding.textJobNumber.text =
                        "Job #$jobId"

                    binding.tvMotherCoil.setText(inputBarcode)

                    binding.tvBatchNumber.setText(
                        String.format("%.3f Tons", motherzincWeight)
                    )

                    binding.tvInputWeight.text =
                        String.format("%.3f Tons", motherWeight)

                    binding.tvZincWeight.text =
                        String.format("%.3f Kg", zincWeight)

                    binding.tvAllowance.text =
                        "${response.allowedToleranceWeightKg} Kg"

                    binding.tvScrap.text =
                        "${response.allowedScrapWeightKg} Kg"

                    binding.tvOutput.text =
                        "${response.allowedOutputWeightInTons} Tons"



                    jobList.clear()

                    if (!response.galvanizingTransactionDetails.isNullOrEmpty()) {

                        response.galvanizingTransactionDetails?.forEach { detail ->

                            val row =
                                GalvanizingTransactionResponse(

                                    galvanizingTranId =
                                    response.galvanizingTranId,

                                    locationId =
                                    response.locationId,

                                    sourceStockId =
                                    response.sourceStockId,

                                    jobNumber =
                                    response.jobNumber,

                                    motherBarcode =
                                    response.motherBarcode,

                                    motherCoilWeight =
                                    response.motherCoilWeight,

                                    materialCode =
                                    response.materialCode,

                                    zincMaterialCode =
                                    response.zincMaterialCode,

                                    zincWeight =
                                    response.zincWeight,

                                    galvanizingTransactionDetails =
                                    mutableListOf(detail)
                                )

                            jobList.add(row)
                        }

                    } else {

                        jobList.add(response)
                    }

                    adapterGp.notifyDataSetChanged()

                    updateUI()
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Utils.showErrorDialog(
                        this,
                        res.message ?: "Error"
                    )
                }

                else -> {}
            }
        }
        bomViewModel.bomLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    val outputs =
                        res.data?.flatMap { it.boMOutput }
                            ?: emptyList()

                    if (
                        outputs.isNotEmpty()
                        &&
                        pendingPosition != -1
                    ) {

                        Utils.showSearchableOutputDialog(
                            activity = this,
                            items = outputs
                        ) { selected ->

                            adapterGp.setSelectedOutput(
                                pendingPosition,
                                selected
                            )
                        }
                    }
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Utils.showErrorDialog(
                        this,
                        res.message ?: "Failed"
                    )
                }

                else -> {}
            }
        }
        gpViewModel.gpAddChildLiveData.observe(this) { res ->

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

                    gpViewModel.fetchGpTransactionById(transactionId)
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
        gpViewModel.gpDeleteChildLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    Toasty.success(
                        this,
                        res.data?.responseMessage ?: "Deleted"
                    ).show()

                    val position = pendingDeletePosition

                    if (
                        position >= 0 &&
                        position < jobList.size
                    ) {

                        jobList.removeAt(position)

                        adapterGp.notifyItemRemoved(position)

                        adapterGp.notifyItemRangeChanged(
                            position,
                            jobList.size
                        )

                        updateUI()
                    }

                    pendingDeletePosition = -1
                }

                is Resource.Error -> {

                    progress.dismiss()

                    pendingDeletePosition = -1

                    Toasty.error(
                        this,
                        res.message ?: "Delete failed"
                    ).show()
                }

                else -> {}
            }
        }
        gpViewModel.initiateGpLiveData.observe(this) { res ->

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

                    Utils.showErrorDialog(
                        this,
                        res.message ?: "Submission failed"
                    )
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

                    // OPTIONAL:
                    // Call physical printer here

                    // finish()
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
        printlabelViewModel.gsmLiveData.observe(this) { res ->

            when (res) {

                is Resource.Loading -> {

                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    val gsmList =
                        res.data?.map {
                            it.gsm.toString()
                        } ?: emptyList()

                    val gsmAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        gsmList
                    )

                    binding.etGps.setAdapter(gsmAdapter)
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toasty.error(
                        this,
                        res.message ?: "Failed to load GSM"
                    ).show()
                }

                else -> {}
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitGP() {

        val updatedList = adapterGp.getUpdatedList()

        val gradeInput =
            binding.etGrade.text.toString().trim()

        val gsmField =
            binding.etGps.text.toString().trim()

        val temperField =
            binding.etTemper.text.toString().trim()

        if (gradeInput.isEmpty()) {

            Utils.showErrorDialog(
                this,
                "Grade is required"
            )
            return
        }

        if (gsmField.isEmpty()) {

            Utils.showErrorDialog(
                this,
                "GSM is required"
            )
            return
        }

        if (temperField.isEmpty()) {

            Utils.showErrorDialog(
                this,
                "Temper is required"
            )
            return
        }

        val detailList =
            mutableListOf<GalvanizingTransactionDetailRequest>()

        var totalComponentWeight = 0.0

        updatedList.forEach { job ->

            job.galvanizingTransactionDetails?.forEach { detail ->

                val componentList =
                    mutableListOf<GalvanizingComponentRequest>()

                detail.components?.forEach { comp ->

                    val isZincMatched =
                        comp.componentCode
                            ?.trim()
                            ?.equals(
                                zincMaterailCode.trim(),
                                ignoreCase = true
                            ) == true

                    val finalWeight =
                        if (isZincMatched) {
                            zincWeight
                        } else {
                            comp.weight ?: 0.0
                        }

                    totalComponentWeight += finalWeight

                    componentList.add(
                        GalvanizingComponentRequest(
                            materialCode = comp.componentCode,
                            weight = finalWeight,
                            uoM = comp.Uom ?: "Kg",
                            isZincComponent = isZincMatched
                        )
                    )
                }

                detailList.add(
                    GalvanizingTransactionDetailRequest(

                        galvanizingTransactionDetailsId =
                        detail.galvanizingTransactionDetailsId ?: 0,

                        barcode =
                        detail.barcode ?: "",

                        materialCode =
                        detail.selectedOutputMaterial
                            ?.outputMaterial ?: "",

                        weightAfterGalvanizing =
                        detail.weightAfterGalvanizing ?: 0.0,

                        uoM = "Tons",

                        weightTakenBy =
                        session.getUserDetails()["userName"]
                            .toString(),

                        weightDateTime =
                        Utils.getCurrentDateTimeISO(),

                        galvanizingComponent =
                        componentList
                    )
                )
            }
        }

        val request =
            GalvanizingTransactionRequest(

                galvanizingTranId = transactionId,

                locationId = locationId,

                sourceStockId = sourceStockId,

                jobNumber = jobId,

                status = "Completed",

                remarks = "Gp is completed",

                isDivided = false,

                inputBarcode = inputBarcode,

                inputWeight = motherzincWeight,

                zincWeight = zincWeight,

                ironLossWeight = 0.0,

                scrapWeight = totalComponentWeight,

                completedBy =
                session.getUserDetails()["userName"]
                    .toString(),

                completedDate =
                Utils.getCurrentDateTimeISO(),

                process = selectedProcess,

                machineName = selectedMachineName,

                tamper = temperField,

                grade = gradeInput,

                gsm = gsmField.toDoubleOrNull(),

                galvanizingTransactionDetails = detailList
            )

        Log.d("FINAL_REQUEST", request.toString())

        gpViewModel.initiateGp(request)
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

    private fun updateUI() {

        binding.recyclerOutput.visibility =
            if (jobList.isEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }
}