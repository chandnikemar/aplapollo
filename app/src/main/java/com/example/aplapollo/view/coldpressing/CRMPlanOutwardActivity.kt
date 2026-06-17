package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.view.BaseScanActivity
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanViewModelfactory
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanvViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmplanOutwardBinding
import es.dmoral.toasty.Toasty

class CRMPlanOutwardActivity : BaseScanActivity() {
    private lateinit var binding: ActivityCrmplanOutwardBinding
    private lateinit var slittingWithoutplanvViewModel: SlittingWithoutplanvViewModel
    private  lateinit var crmViewModel: CRMViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    //        private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var locationId:Int=0
    private var sourceStockId: Int = 0
    private var scannedBarcode: String? = null
    private  var tenantCode:String?=null
    private var transactionId:Int=0
    private var maxAllowedWidth: Double = 0.0
    private  var weight:Double=0.0
    private var coilThickness: Double = 0.0
    private var headerTittle:String=""
private var headerTittleCRCA:String=""
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    override fun onBarcodeScanned(barcode: String) {

        runOnUiThread {

            Log.d("SCAN_DEBUG", "Scanned Barcode = $barcode")


            binding.commanInputRow.inputField.setText(barcode)


            binding.commanInputRow.inputField.setSelection(barcode.length)


            scannedBarcode = barcode

            // Call API automatically

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_crmplan_outward)
        binding.commanInputRow.inputField.apply {

            requestFocus()

            isFocusable = true
            isFocusableInTouchMode = true

            post {
                requestFocus()

            }
        }

        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactory = SlittingWithoutplanViewModelfactory(application, retrofitInstance)
        slittingWithoutplanvViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingWithoutplanvViewModel::class.java]
        val viewModelProviderFactorys = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, viewModelProviderFactorys)[CRMViewModel::class.java]

        session = SessionManager(this)
        userDetail = session.getUserDetails()
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
//                tenantCode= userDetail!![SessionManager.Key_tenantCode].toString()
            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()
            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"
            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        locationId=intent.getIntExtra(LocationId,0)
        headerTittle=intent.getStringExtra("FIRST_PAGECRFH")?:""
        headerTittleCRCA=intent.getStringExtra("FIRST_PAGECRCA")?:""
        selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
//        binding.TvItemThickness.visibility=View.GONE
        binding.idLayoutHeader.tvTitle.text="$selectedProcessName Planning"
        binding.idLayoutHeader.tvSubtitle.text="Generate production plans from scanned coils"

        binding.layoutBatchDetails.visibility = View.GONE


        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.commanInputRow.btnSearch.setOnClickListener {

            val barcode = binding.commanInputRow.inputField.text.toString().trim()

            if (barcode.isEmpty()) {
                Toasty.warning(this, "Please scan barcode").show()
                return@setOnClickListener
            }

            slittingWithoutplanvViewModel
                .getStockByBatchOrBarcode( barcode)
        }

//        binding.etDesiredThickness.addTextChangedListener {
//
//            val entered = it.toString().toDoubleOrNull() ?: return@addTextChangedListener
//
//            if (entered >= coilThickness) {
//
//                Toasty.error(
//                    this,
//                    "Thickness must be less than $coilThickness mm"
//                ).show()
//
//                binding.etDesiredThickness.setText("")
//            }
//        }

        slittingWithoutplanvViewModel.stockByBarcodeLiveData.observe(this) { resource ->

            when (resource) {

                is Resource.Loading -> {
                    Log.d("CRM_WithoutPLAN_3", "Loading stock")
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()

                    val stock = resource.data ?: return@observe
                    if (stock == null) {

                        Toasty.error(
                            this,
                            "No transaction data found"
                        ).show()

                        return@observe
                    }
                    Log.d("CRM_WithoutPLAN_3", "Stock = $stock")

                    binding.layoutBatchDetails.visibility = View.VISIBLE
                    binding.layoutActionButtons.visibility = View.VISIBLE
                    binding.inCRMBatch.tvItemCode.text =
                        "${stock.materialCode}"

                    binding.inCRMBatch.tvSupplierBatchNo.text =
                        "${stock.supplierBatchNo}"

                    binding.inCRMBatch.tvGrade.text =
                        "${stock.grade}"

                    binding.inCRMBatch.tvWidth.text =
                        "%.3f".format(stock?.width.toString().toDoubleOrNull() ?: 0.0)
                    binding.inCRMBatch.tvThickness.text =
                        "%.2f".format(stock?.thickness.toString().toDoubleOrNull() ?: 0.0)


                    binding.inCRMBatch.tvWeight.text =
                        "%.3f".format(stock?.weight.toString().toDoubleOrNull() ?: 0.0)
                    sourceStockId = stock.stockId
                    scannedBarcode = stock.barcode
                    tenantCode = stock.tenantCode
                    transactionId = stock.transactionId ?: 0
                    maxAllowedWidth = stock.width ?: 0.0
                    weight = stock.weight ?: 0.0
                    coilThickness = stock.thickness?:0.0
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Utils.showErrorDialog(this, resource.message ?: "Error",)
//                    Log.e("SLITTING_PLAN_3", "Error = ${resource.message}")
//                    Toasty.error(this, resource.message ?: "Error").show()
                }

                else -> {}
            }
        }

        crmViewModel.initiateCRMWithoutPlanLiveData
            .observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        Toasty.success(
                            this,
                            "initiated successfully"
                        ).show()
                        Log.d("CRM_WithoutPLAN_3", """
                    API Success
                    Response = ${resource.data}
                """.trimIndent())

                        finish() // or navigate
                    }

                    is Resource.Error -> {
                        progress.dismiss()

                        Toasty.error(
                            this,
                            resource.message ?: "Failed to initiate"
                        ).show()
                    }
                }
            }


        binding.btncSaves.setOnClickListener {

            if (sourceStockId == 0) {
                Toasty.warning(this, "Please scan coil first").show()
                return@setOnClickListener
            }

//            val enteredWidth =
//                binding.etDesiredThickness.text.toString().toDoubleOrNull()

//            if (enteredWidth == null || enteredWidth <= 0) {
//                Toasty.warning(this, "Please enter valid Thickness").show()
//                return@setOnClickListener
//            }

//            if (enteredWidth > maxAllowedWidth) {
//                Toasty.error(
//                    this,
//                    "Thickness cannot be greater than $maxAllowedWidth mm"
//                ).show()
//                return@setOnClickListener
//            }

            val request = CRMTransactionRequest(
                crmTranId = 0,

//                tenantCode = tenantCode,
                crmPlanId=0,
                locationId=locationId,
                sourceStockId=sourceStockId,
                desiredThickness=0.0,
                weight = null,
                jobNumber="",
                inputBarcode = null,
                inputWeight = null,
//                barcode=scannedBarcode,
                materialCode = null,
                ironLossWeight=null,
                scrapWeight=null,
//                weightAfterCRM=null,
                isCoilDivided = false,
//                dividedCRMTranId=null,
                completedBy ="",
                completedDate = null,
                status="InProgress",
                remarks="Transaction",
                isPlanned=false,
                process=selectedProcessName,
                machineName = null,
                tamper = null,
                grade=null,
                crmTransactionDetails =null

            )

            crmViewModel.initiateCRMWithoutPlan(request)
        }
        binding.commanInputRow.btnClear.setOnClickListener {

            binding.commanInputRow.inputField.text?.clear()
//            binding.layoutBatchDetails.removeAllViews()
//            binding.etDesiredThickness.text?.clear()

            // Hide batch details
            binding.layoutBatchDetails.visibility = View.GONE

//            // Remove all dynamic weight rows
//            binding.layoutWeightContainer.removeAllViews()

            // Reset variables
            sourceStockId = 0
            scannedBarcode = null
            tenantCode = null
            transactionId = 0
            maxAllowedWidth = 0.0
            coilThickness = 0.0

            sourceStockId = 0
            scannedBarcode = null
        }
        binding.btncClears.setOnClickListener {

            binding.commanInputRow.inputField.text?.clear()


            // Hide batch details
            binding.layoutBatchDetails.visibility = View.GONE



            // Reset variables
            sourceStockId = 0
            scannedBarcode = null
            tenantCode = null
            transactionId = 0
            maxAllowedWidth = 0.0
            coilThickness = 0.0

            sourceStockId = 0
            scannedBarcode = null
        }


    }









}