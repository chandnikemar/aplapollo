package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.InsStockStatus
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Constants.LocationName
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.CRM.CRMPlanResponse
import com.example.aplapollo.model.CRM.CRMTransactionRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmplanBinding
import es.dmoral.toasty.Toasty

class CRMPlanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmplanBinding
    private lateinit var progress: ProgressDialog
    private  lateinit var crmViewModel: CRMViewModel
    private lateinit var slittingViewModel: SlittingViewModel
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var scannedStockId: Int? = null
    private var locationId: Int = 0
    private var planList = listOf<CRMPlanResponse>()
    private lateinit var planAdapter: ArrayAdapter<String>
    private var selectedPlanDetail: CRMPlanResponse? = null
private  var weight:Double=0.0
    private var locationName: String = ""
    private var scanBuffer = StringBuilder()
    private var lastKeyTime = 0L
    private val SCAN_TIMEOUT = 300L
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmplan    )
        binding.idLayoutHeader.tvTitle.text = "CRM Plan"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactorys = SlittingViewModelfactory(application, retrofitInstance)
        slittingViewModel = ViewModelProvider(this, viewModelProviderFactorys)[SlittingViewModel::class.java]
        val viewModelProviderFactory = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, viewModelProviderFactory)[CRMViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        locationId = intent.getIntExtra(LocationId, 0)
        locationName = intent.getStringExtra(LocationName) ?: ""
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

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        var lastScanTime = 0L
        val SCAN_DELAY = 300L
        binding.etScanCoil.hint = "Scan here"

        binding.etScanCoil.requestFocus()
        crmViewModel.getCRMPlannedList()
        crmViewModel.crmPlanMutableLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    // show progress
                }

                is Resource.Success -> {
                    resource.data?.let { list ->
                        planList = list
                        setupPlanDropdown(list)
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
        crmViewModel.CrmPlanDetailLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()
                    resource.data?.let { planDetail ->
                        val firstDetail = planDetail.crmPlanDetail.firstOrNull()

                        if (firstDetail == null) {

                            Toasty.warning(this, "No CRM plan detail found").show()
                            binding.etDesiredWeight.setText("")

                            return@observe
                        }
                        selectedPlanDetail = planDetail
                        val widths = resource.data?.crmPlanDetail
                            ?.map { it.requiredCoilWidth }
                            ?: emptyList()
                        // Show the sections
                        binding.layoutInputRequirement.visibility = View.VISIBLE
                        binding.layoutSlittingPlan.visibility = View.VISIBLE
                        binding.layoutScanCoil.visibility = View.VISIBLE
                        binding.layoutScanDetails.visibility= View.VISIBLE


                        // Bind the data
                        binding.Tvitem.text = "Item: ${planDetail.materialCode}"


                        binding.tvGrade.text = "Grade: ${planDetail.grade}"
                        binding.tvWidth.text = "Width: ${planDetail.width} MM"
                        binding.tvthickness.text = "Thickness: ${planDetail.thickness} MM"
                        binding.etDesiredWeight.setText(
                            firstDetail.requiredCoilWidth?.toString() ?: ""
                        )
                        // Bind TableLayout for hrSlittingPlanDetail

                        activateScanUI()
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, resource.message ?: "Failed to load plan details", Toasty.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
        crmViewModel.crmScanLiveData.observe(this) { resource ->

            when (resource) {

                is Resource.Loading -> {

                    progress.show()

                    // 🔴 Hide scan immediately when API starts
                    hideScanBox()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    val stock = resource.data?.responseObject


                    if (stock?.stockId == null || stock.stockId == 0) {

                        showScanBox()

                        // Red border
                        binding.tilScanCoil.boxStrokeColor =
                            ContextCompat.getColor(this, android.R.color.holo_red_dark)

                        Toasty.error(this, "Invalid barcode", Toasty.LENGTH_SHORT).show()
                        return@observe
                    }

                    // ✅ Valid Barcode
                    scannedStockId = stock.stockId

                    // Hide scan, show data
                    hideScanBox()
                    showBatchDetails()
                    weight = stock.weight ?: 0.0
                    // Green border
                    binding.tilScanCoil.boxStrokeColor =
                        ContextCompat.getColor(this, android.R.color.holo_green_dark)

                    // Bind Data
                    binding.tvscBatch.text = "Barcode: ${stock.barcode}"
                    binding.tvscQcDate.text = "Grade: ${stock.grade}"
                    binding.tvscSupplier.text = "Weight: ${stock.weight}"
                    binding.tvscWeight.text = "Supplier No: ${stock.supplierBatchNo}"
                    binding.tvscGrnDate.text = "Thickness: ${stock.thickness}"
                    binding.tvscWidth.text = "Width: ${stock.width}"
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toasty.error(
                        this,
                        resource.message ?: "Scan failed",
                        Toasty.LENGTH_SHORT
                    ).show()

                    // 🔁 Back to scan mode
                    showScanBox()

                    // Red border
                    binding.tilScanCoil.boxStrokeColor =
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)
                }

                else -> {}
            }
        }
        slittingViewModel.hrItemAgainstPlanLiveData.observe(this) { resource ->

            when (resource) {

                is Resource.Success -> {

                    val stockList = resource.data ?: emptyList()

                    if (stockList.isEmpty()) {
                        Toasty.info(this, "No stock available").show()
                        return@observe
                    }

                    showViewStockPopup(stockList)

                    //                        binding.spinnerViewStock.setAdapter(stockAdapter)
                    //                        binding.spinnerViewStock.showDropDown() // 🔥 open automatically
                }

                is Resource.Error -> {
                    Toasty.error(this, resource.message ?: "Failed to load stock", Toasty.LENGTH_SHORT).show()
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
                        response?.responseMessage ?: "Slitting initiated",
                        Toasty.LENGTH_SHORT
                    ).show()

//                    Log.d("SLITTING_INIT", "TranId=$tranId JobNo=$jobNo")
                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(
                        this,
                        resource.message ?: "Failed to initiate slitting",
                        Toasty.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }

        binding.tilSelectPlan.defaultHintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))

        binding.tilSelectPlan.hintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))

        binding.tilSelectPlan.setEndIconTintList(
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        )
        binding.spinnerSelectPlan.setOnItemClickListener { _, _, position, _ ->

            val selectedPlanNo = planAdapter.getItem(position)

            val selectedPlan = planList.firstOrNull {
                it.crmPlanNo == selectedPlanNo
            }

            selectedPlan?.let { plan ->

                crmViewModel.getCRMPlanDetailById(selectedPlan.crmPlanId)
            }

        }

        binding.btnViewStock.setOnClickListener {
            val plan = selectedPlanDetail
            if (plan == null) {
                Toasty.warning(this, "Please select plan first", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val request= HrSlittingItemAgainstPlanRequest(
                grade = plan.grade ?: "",
                width = plan.width ?: 0.0,
                thickness = plan.thickness ?: 0.0,
                Status= InsStockStatus
            )
            slittingViewModel.getAllItemAgainstPlan( request)

        }


        binding.etScanCoil.addTextChangedListener(object : android.text.TextWatcher {


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lastScanTime = System.currentTimeMillis()
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                s ?: return

                val barcode = s.toString().trim()
                if (barcode.length < 5) return // avoid partial input

                //    binding.etScanCoil.removeCallbacks(scanRunnable)
                binding.etScanCoil.postDelayed(scanRunnable, SCAN_DELAY)
            }

            val scanRunnable = Runnable {
                val barcode = binding.etScanCoil.text.toString().trim()

                // ✅ Validate
                if (barcode.isEmpty()) return@Runnable

                if (selectedPlanDetail == null) {
                    Toasty.warning(
                        this@CRMPlanActivity,
                        "Please select plan first",
                        Toasty.LENGTH_SHORT
                    ).show()
                    binding.etScanCoil.text?.clear()
                    binding.etScanCoil.requestFocus()
                    return@Runnable
                }


                crmViewModel.getCRMScan(

                    barcode,
                    selectedPlanDetail!!.crmPlanId
                )
            }
        })


        binding.btnSubmit.setOnClickListener {

            if (locationId <= 0) {
                Toasty.warning(
                    this,
                    "Please select location first",
                    Toasty.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (binding.spinnerSelectPlan.text.isNullOrEmpty()
                || binding.spinnerSelectPlan.text.toString() == "-- Select Plan --"
            ) {
                Toasty.warning(this, "Please select plan first", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPlan = planList.firstOrNull {
                it.crmPlanNo == binding.spinnerSelectPlan.text.toString()
            }
            if (scannedStockId == null || scannedStockId == 0) {
                Toasty.warning(this, "Please scan coil first").show()
                return@setOnClickListener
            }

            val desiredThickness =
                binding.etDesiredWeight.text.toString().toDoubleOrNull()
            if (desiredThickness == null || desiredThickness <= 0) {
                Toasty.warning(this, "Enter valid desired thickness").show()
                return@setOnClickListener
            }
            if (desiredThickness == null || desiredThickness <= 0) {
                Toasty.warning(this, "Enter valid desired thickness").show()
                return@setOnClickListener
            }
//            val barcode = binding.etScanCoil.text.toString().trim()
            if (selectedPlan == null) {
                Toasty.error(this, "Invalid plan selected", Toasty.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = CRMTransactionRequest(
                crmTranId = 0,

                crmPlanId=selectedPlanDetail!!.crmPlanId,
                tenantCode=tenantCode,
                locationId=locationId,
                sourceStockId=scannedStockId?:0,
                desiredThickness=desiredThickness,
                Weight=weight,
                jobNumber="",
                barcode="",
                ironLossWeight=null,
                scrapWeight=null,
                weightAfterCRM=null,
                isCoilDivided = false,
                dividedCRMTranId=null,
                completedBy = "",
                completedDate="",
                status="",
                remarks="",
                isPlanned=true

            )

            // ✅ API CALL
            crmViewModel.processCRM( request)
        }
        binding.btnClear.setOnClickListener {
            resetAllUI()
        }



//        binding.btnSubmit.setOnClickListener {
//            startActivity(Intent(this@CRMPlanActivity, CRMTransactionActivity::class.java))
//        }
    }
    private fun resetAllUI() {

        // Clear scanned id
        scannedStockId = null

        // Clear scan text
        binding.etScanCoil.setText("")

        // Reset plan selection
        binding.spinnerSelectPlan.setText("-- Select Plan --", false)

        // Clear selected plan
        selectedPlanDetail = null

        // Hide all sections
        binding.layoutScanDetails.visibility = View.GONE
        binding.layoutBatchDetails.visibility = View.GONE
        binding.layoutButtons.visibility = View.GONE
        binding.layoutInputRequirement.visibility = View.GONE
        binding.layoutSlittingPlan.visibility = View.GONE
        binding.layoutScanCoil.visibility = View.GONE

        // Reset border color
        binding.tilScanCoil.boxStrokeColor =
            ContextCompat.getColor(this, androidx.appcompat.R.color.material_blue_grey_900  )

        // Clear adapter data


        // Clear text fields
        binding.Tvitem.text = ""
        binding.tvGrade.text = ""
        binding.tvWidth.text = ""
        binding.tvthickness.text = ""

        // Hide keyboard
        hideKeyboard()

        // Focus on dropdown
        binding.spinnerSelectPlan.requestFocus()
    }
    private fun setupPlanDropdown(list: List<CRMPlanResponse>) {

        val planNumbers = list.mapNotNull { it.crmPlanNo}

        planAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            planNumbers
        )

        binding.spinnerSelectPlan.setAdapter(planAdapter)
        binding.spinnerSelectPlan.setText("-- Select Plan --", false)
        binding.spinnerSelectPlan.keyListener = null // makes EditText read-only
    }
    private fun showViewStockPopup(stockList: List<HrSlittingItemAgainstPlanResponse>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_view_stock, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.containerStockRows)


        stockList.forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 4
                }
            }

            val barcodeView = TextView(this).apply {
                text = item.barcode ?: "-"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val weightView = TextView(this).apply {
                text = item.weight?.toString() ?: "-"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            row.addView(barcodeView)
            row.addView(weightView)
            container.addView(row)
        }

        AlertDialog.Builder(this)
            .setTitle("View Stock")
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }


    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {

        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()

            // reset buffer if typing delay is big (manual typing)
            if (now - lastKeyTime > SCAN_TIMEOUT) {
                scanBuffer.clear()
            }
            lastKeyTime = now

            val char = event.unicodeChar.toChar()
            if (char.code > 0) {
                scanBuffer.append(char)
            }

            // Most scanners send ENTER at end
            if (event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                val barcode = scanBuffer.toString().trim()
                scanBuffer.clear()

                if (barcode.isNotEmpty()) {
                    handleScannedBarcode(barcode)
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
    private fun handleScannedBarcode(barcode: String) {

        Log.d("SCANNER", "Barcode = $barcode")

        if (barcode.isEmpty()) return

        // Show in UI
        binding.etScanCoil.setText(barcode)

        // Disable while API runs (avoid double scan)
        binding.etScanCoil.isEnabled = false

        if (selectedPlanDetail == null) {

            Toasty.warning(this, "Please select plan first").show()

            showScanBox()
            return
        }

        crmViewModel.getCRMScan(
            barcode,
            selectedPlanDetail!!.crmPlanId
        )
    }

    private fun activateScanUI() {

        // Show scan ONLY if batch is not visible
        if (binding.layoutBatchDetails.visibility == View.VISIBLE) {
            return
        }

        binding.layoutScanDetails.visibility = View.VISIBLE

        binding.etScanCoil.apply {
            isEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            setText("")
        }

        hideKeyboard()

        binding.tilScanCoil.boxStrokeColor =
            ContextCompat.getColor(this, es.dmoral.toasty.R.color.material_deep_teal_500)
    }


    private fun showScanBox() {

        binding.layoutScanDetails.visibility = View.VISIBLE

        binding.etScanCoil.apply {
            isEnabled = true
            setText("")
            requestFocus()
        }

        hideKeyboard()

        binding.layoutBatchDetails.visibility = View.GONE
        binding.layoutButtons.visibility = View.GONE

        // Active border
        binding.tilScanCoil.boxStrokeColor =
            ContextCompat.getColor(this, androidx.appcompat.R.color.material_deep_teal_200)
    }





    private fun showBatchDetails() {
        binding.layoutScanDetails.visibility = View.GONE
        binding.layoutBatchDetails.visibility = View.VISIBLE
        binding.layoutButtons.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etScanCoil.windowToken, 0)
    }



    private fun hideScanBox() {

        binding.layoutScanDetails.visibility = View.GONE
        binding.etScanCoil.apply {
            isEnabled = false
            clearFocus()
        }

    }
}