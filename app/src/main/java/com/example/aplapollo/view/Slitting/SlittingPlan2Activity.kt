    package com.example.aplapollo.view.Slitting

    import android.app.ProgressDialog
    import android.content.Intent
    import android.content.res.ColorStateList
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.LinearLayout
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AlertDialog
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.ViewModelProvider
    import androidx.recyclerview.widget.LinearLayoutManager
    import com.example.aplapollo.adapter.Slitting.SlittingWidthAdapter
    import com.example.aplapollo.helper.Constants
    import com.example.aplapollo.helper.LogoutHelper
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionExpiredEvent
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanRequest
    import com.example.aplapollo.model.Slitting.HrSlittingItemAgainstPlanResponse
    import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
    import com.example.aplapollo.model.Slitting.InitiateSlittingRequest
    import com.example.aplapollo.repository.APLRepository
    import com.example.aplapollo.view.LoginActivity
    import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
    import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
    import com.example.apolloapl.R
    import com.example.apolloapl.databinding.ActivitySlittingPlan2Binding
    import es.dmoral.toasty.Toasty

    class SlittingPlan2Activity : AppCompatActivity() {
        private lateinit var binding: ActivitySlittingPlan2Binding
        private lateinit var slittingViewModel: SlittingViewModel
        private lateinit var progress: ProgressDialog
        private lateinit var session: SessionManager
        private var baseUrl: String = ""
        private var userName: String? = ""
        private var token: String? = ""
        private  var tenantCode:String?=""
        private  var userDetail: HashMap<String, Any?>?=null
        private var serverIpSharedPrefText: String? = null
        private var serverHttpPrefText: String? = null
        private lateinit var planAdapter: ArrayAdapter<String>
        private var planList = listOf<HrSlittingPlanResponse>()
        private lateinit var slittingWidthAdapter: SlittingWidthAdapter

        private var selectedPlanDetail: HrSlittingPlanResponse? = null

        private var scannedStockId: Int? = null
        private var locationId: Int = 0
        private var locationName: String = ""
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = DataBindingUtil.setContentView(this
                    ,R.layout.activity_slitting_plan2)
            binding.idLayoutHeader.tvTitle.text = "Slitting From Plan "
            supportActionBar?.hide()
                progress = ProgressDialog(this)
                progress.setMessage("Please Wait...")
                val aplRepository = APLRepository()
            val viewModelProviderFactory = SlittingViewModelfactory(application, aplRepository)
            slittingViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingViewModel::class.java]
            slittingWidthAdapter = SlittingWidthAdapter()
            session = SessionManager(this)
            userDetail = session.getUserDetails()
            locationId = intent.getIntExtra("LOCATION_ID", 0)
            locationName = intent.getStringExtra("LOCATION_NAME") ?: ""
            Log.d("RECEIVED_LOCATION", "Id=$locationId Name=$locationName")
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


            SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
                if (shouldLogout == true) {
                    SessionExpiredEvent.logoutLiveData.value = false
                    LogoutHelper.handleLogout(this, session)
                }
            }
            slittingViewModel.getHrSlittingPlannedList(baseUrl)




            slittingViewModel.hrSlittingPlanMutableLiveData.observe(this) { resource ->
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
            slittingViewModel.hrSlittingPlanDetailLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()
                        resource.data?.let { planDetail ->
                            selectedPlanDetail = planDetail
                            val widths = resource.data?.hrSlittingPlanDetail
                                ?.map { it.requiredCoilWidth }
                                ?: emptyList()
                            // Show the sections
                            binding.layoutInputRequirement.visibility = View.VISIBLE
                            binding.layoutSlittingPlan.visibility = View.VISIBLE
                            binding.layoutScanCoil.visibility = View.VISIBLE
                            binding.layoutScanDetails.visibility=View.VISIBLE


                            // Bind the data
                            binding.Tvitem.text = "Item: ${planDetail.materialCode}"
                            binding.tvGrade.text = "Grade: ${planDetail.grade}"
                            binding.tvWidth.text = "Width: ${planDetail.width} MM"
                            binding.tvthickness.text = "Thickness: ${planDetail.thickness} MM"

                            // Bind TableLayout for hrSlittingPlanDetail

                            binding.rvSlittingWidths.apply {
                                adapter = slittingWidthAdapter
                                layoutManager = LinearLayoutManager(
                                    context,
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                            }
                            slittingWidthAdapter.submitList(widths)
                        }
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        Toasty.error(this, resource.message ?: "Failed to load plan details", Toasty.LENGTH_SHORT).show()
                    }

                    else -> {}
                }
            }

            slittingViewModel.hrSlittingScanLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        val stock = resource.data?.responseObject

                        if (stock?.stockId == null || stock.stockId == 0) {

                            binding.layoutBatchDetails.visibility = View.GONE
                            binding.layoutScanDetails.visibility = View.VISIBLE

                            Toasty.error(this, "Invalid barcode", Toasty.LENGTH_SHORT).show()
                            return@observe
                        }

                        scannedStockId = stock.stockId
                        binding.layoutScanDetails.visibility = View.GONE
                        binding.layoutBatchDetails.visibility = View.VISIBLE
                        binding.layoutButtons.visibility = View.VISIBLE

                        binding.tvscBatch.text = "Barcode: ${stock.barcode}"
                        binding.tvscQcDate.text = "Grade: ${stock.grade}"
                        binding.tvscSupplier.text = "Weight: ${stock.weight}"
                        binding.tvscWeight.text="Supplier No:${stock.supplierBatchNo}"
                        binding.tvscGrnDate.text = "Thickness: ${stock.thickness}"
                        binding.tvscWidth.text = "Width: ${stock.width}"
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        binding.etScanCoil.removeCallbacks(null)

                        binding.layoutBatchDetails.visibility = View.GONE
                        binding.layoutButtons.visibility = View.GONE
                        binding.layoutScanDetails.visibility = View.VISIBLE
                        Toasty.error(
                            this,
                            resource.message ?: "Scan failed",
                            Toasty.LENGTH_SHORT
                        ).show()
                        binding.etScanCoil.text?.clear()
                        binding.etScanCoil.requestFocus()
//
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

                        // 🔥 Show popup with actual data from API
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
            slittingViewModel.initiateSlittingLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Loading -> {
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        val response = resource.data
                        val tranId = response?.responseObject?.hrSlittingTranId
                        val jobNo = response?.responseObject?.jobNumber

                        Toasty.success(
                            this,
                            response?.responseMessage ?: "Slitting initiated",
                            Toasty.LENGTH_SHORT
                        ).show()

                        Log.d("SLITTING_INIT", "TranId=$tranId JobNo=$jobNo")
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
                    it.hrSlittingPlanNo == selectedPlanNo
                }

                selectedPlan?.let { plan ->

                    slittingViewModel.getHrSlittingPlanById(baseUrl,selectedPlan.hrSlittingPlanId)
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
                    thickness = plan.thickness ?: 0.0
                )
                slittingViewModel.getAllItemAgainstPlan(baseUrl, request)

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

                    binding.etScanCoil.removeCallbacks(scanRunnable)
                    binding.etScanCoil.postDelayed(scanRunnable, SCAN_DELAY)
                }

                val scanRunnable = Runnable {
                    val barcode = binding.etScanCoil.text.toString().trim()

                    // ✅ Validate
                    if (barcode.isEmpty()) return@Runnable

                    if (selectedPlanDetail == null) {
                        Toasty.warning(
                            this@SlittingPlan2Activity,
                            "Please select plan first",
                            Toasty.LENGTH_SHORT
                        ).show()
                        binding.etScanCoil.text?.clear()
                        binding.etScanCoil.requestFocus()
                        return@Runnable
                    }


                    slittingViewModel.getHrSlittingScan(
                        baseUrl,
                        barcode,
                        selectedPlanDetail!!.hrSlittingPlanId
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
                    it.hrSlittingPlanNo == binding.spinnerSelectPlan.text.toString()
                }

                if (selectedPlan == null) {
                    Toasty.error(this, "Invalid plan selected", Toasty.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val request = InitiateSlittingRequest(
                    HRSlittingTranId = 0,
                    TenantCode = selectedPlan.tenantCode ?: "",
                    HRSlittingPlanId = selectedPlan.hrSlittingPlanId ,
                    LocationId = locationId ,          // 🔁 replace if dynamic
                    SourceStockId = scannedStockId?:0,       // 🔁 replace if dynamic
                    IsActive = true,
                    Status = "Draft",
                    Remarks = "Draft"
                )
                Log.d(
                    "SLITTING_SUBMIT_REQ",
                    """
        InitiateSlittingRequest(
            HRSlittingTranId=${request.HRSlittingTranId},
            TenantCode=${request.TenantCode},
            HRSlittingPlanId=${request.HRSlittingPlanId},
            LocationId=${request.LocationId},
            SourceStockId=${request.SourceStockId},
            Status=${request.Status},
            Remarks=${request.Remarks}
        )
        """.trimIndent()
                )
                // ✅ API CALL
                slittingViewModel.initiateHrSlitting(baseUrl, request)
            }


        }

        

        private fun setupPlanDropdown(list: List<HrSlittingPlanResponse>) {

            val planNumbers = list.map { it.hrSlittingPlanNo }

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

        private fun showLogoutPopup() {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    logout()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        }

        private fun logout() {
            session.logoutKeepAdminConfig()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }