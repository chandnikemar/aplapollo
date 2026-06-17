    package com.example.aplapollo.view.slitting

    import android.annotation.SuppressLint
    import android.app.ProgressDialog
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.EditText
    import android.widget.ImageButton
    import androidx.core.widget.addTextChangedListener
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.ViewModelProvider
    import com.example.aplapollo.api.RetrofitInstance
    import com.example.aplapollo.helper.Constants
    import com.example.aplapollo.helper.Constants.LocationId
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.helper.Utils
    import com.example.aplapollo.helper.Utils.showErrorDialog
    import com.example.aplapollo.model.PrintLabelBarcodeRequest
    import com.example.aplapollo.model.Slitting.CoilSplitRequest
    import com.example.aplapollo.model.Slitting.HRSlittingTransactionDetailRequest
    import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
    import com.example.aplapollo.view.BaseScanActivity
    import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
    import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
    import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanViewModelfactory
    import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanvViewModel
    import com.example.apolloapl.R
    import com.example.apolloapl.databinding.ActivitySlittingplan3Binding
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import es.dmoral.toasty.Toasty

    class Slittingplan3Activity : BaseScanActivity() {
        private lateinit var binding: ActivitySlittingplan3Binding
        private lateinit var slittingWithoutplanvViewModel: SlittingWithoutplanvViewModel
        private lateinit var printlabelViewModel: PrintlabelViewModel
        private lateinit var progress: ProgressDialog

        private lateinit var session: SessionManager
        private var baseUrl: String = ""
        private var userName: String? = ""
        private var token: String? = ""

        private  var userDetail: HashMap<String, Any?>?=null
        private var serverIpSharedPrefText: String? = null
        private var serverHttpPrefText: String? = null
        private var locationId:Int=0
        private var sourceStockId: Int = 0
        private var scannedBarcode: String? = null
        private  var tenantCode:String?=null
        private var transactionId:Int=0
        private var maxAllowedWidth: Double = 0.0

        private var selectedProcessName: String = ""
        private var selectedMachineName: String = ""
        private var isEnteringWidth = false
        private var splitBarcode: String? = null
        override fun onBarcodeScanned(barcode: String) {

            // Ignore scanner while entering width
            if (isEnteringWidth) {
                Log.d("SCAN_DEBUG", "Ignored scan while entering width")
                return
            }

            runOnUiThread {

                Log.d("SCAN_DEBUG", "Scanned Barcode = $barcode")

                // Show scanned value in EditText
                binding.commanInputRow.inputField.setText(barcode)

                // Move cursor to end
                binding.commanInputRow.inputField.setSelection(barcode.length)

                // Save barcode
                scannedBarcode = barcode

                // Call API automatically
                slittingWithoutplanvViewModel
                    .getStockByBatchOrBarcode(barcode)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this,R.layout.activity_slittingplan3)
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
            val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
            printlabelViewModel =
                ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]
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
                tenantCode = userDetail!!["defaultTenantCode"].toString()
//                tenantCode= userDetail!![SessionManager.Key_tenantCode].toString()
                serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
                serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()
                baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"
                // ⭐ PRINT TOKEN HERE
                Log.d("JWT_TOKEN_QC", "JWT Token = $token")
                Log.d("Tanent_Code","Tenant Code= $tenantCode")
            }
            locationId=intent.getIntExtra(LocationId,0)
            selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
            selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
            binding.idLayoutHeader.tvTitle.text = "$selectedProcessName Planning"
            binding.idLayoutHeader.tvSubtitle.text="Generate production plans from scanned coils"
//            binding.layoutBatchDetails.visibility = View.GONE
            binding.layoutWeightContainer.removeAllViews()

            binding.idLayoutHeader.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            hidePlanningSection()

            binding.commanInputRow.btnSearch.setOnClickListener {

                val barcode = binding.commanInputRow.inputField.text.toString().trim()

                if (barcode.isEmpty()) {
                    Toasty.warning(this, "Please scan barcode").show()
                    return@setOnClickListener
                }

                slittingWithoutplanvViewModel
                    .getStockByBatchOrBarcode( barcode)
            }
            binding.commanInputRow.btnClear.setOnClickListener {
                binding.commanInputRow.inputField.setText("")

                sourceStockId = 0
                scannedBarcode = null
                transactionId = 0
                maxAllowedWidth = 0.0

                binding.commanInputRow.inputField.requestFocus()

                binding.commanInputRow.inputField.setSelection(
                    binding.commanInputRow.inputField.text.length
                )
            }
            binding.btncClears.setOnClickListener {
                hidePlanningSection()

                binding.commanInputRow.inputField.setText("")

                sourceStockId = 0
                scannedBarcode = null
                transactionId = 0
                maxAllowedWidth = 0.0

                binding.commanInputRow.inputField.requestFocus()

                binding.commanInputRow.inputField.setSelection(
                    binding.commanInputRow.inputField.text.length
                )

            }
            binding.btnAddPlan.setOnClickListener {
                binding.cardWeight.visibility = View.VISIBLE
                if (binding.layoutWeightContainer.childCount == 0) {
                    addWeightRow()
                }
            }
            binding.btnSlit.setOnClickListener {
                showSlitDialog()
            }

            slittingWithoutplanvViewModel.stockByBarcodeLiveData.observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {
                        Log.d("SLITTING_PLAN_3", "Loading stock")
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        val stock = resource.data ?: return@observe

                        Log.d("SLITTING_PLAN_3", "Stock = $stock")

//                        binding.layoutBatchDetails.visibility = View.VISIBLE
                        showPlanningSection()

                        binding.inCommanBatch.tvItemCode.text =
                            "${stock.materialCode}"

                        binding.inCommanBatch.tvSupplierBatchNo.text =
                            "${stock.supplierBatchNo}"

                        binding.inCommanBatch.tvGrade.text =
                            "${stock.grade}"

                        binding.inCommanBatch.tvWidth.text =
                            "%.3f".format(stock?.width.toString().toDoubleOrNull() ?: 0.0)
                        binding.inCommanBatch.tvThickness.text =
                            "%.2f".format(stock?.thickness.toString().toDoubleOrNull() ?: 0.0)

                        binding.inCommanBatch.tvWeight.text =
                            "%.3f".format(stock?.weight.toString().toDoubleOrNull() ?: 0.0)

                        sourceStockId = stock.stockId
                        scannedBarcode = stock.barcode
                        tenantCode=stock.tenantCode
                        transactionId=stock.transactionId?:0
                        maxAllowedWidth = stock.width ?: 0.0
//                        splitBarcode = stock.barcode

                        Toasty.success(this, "Stock fetched successfully").show()
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        showErrorDialog(this,resource.message ?: "Error",)
                        Log.e("SLITTING_PLAN_3", "Error = ${resource.message}")
//                        Toasty.error(this, resource.message ?: "Error").show()
                    }

                    else -> {}
                }
            }

            slittingWithoutplanvViewModel.initiateSlittingWithoutPlanLiveData
                .observe(this) { resource ->

                    when (resource) {

                        is Resource.Loading -> {
                            progress.show()
                        }

                        is Resource.Success -> {
                            progress.dismiss()

                            val response = resource.data


                            val barcode = binding.commanInputRow.inputField.text.toString().trim()

                            slittingWithoutplanvViewModel
                                .getStockByBatchOrBarcode(barcode)
                            finish()
                        }
                        is Resource.Error -> {
                            progress.dismiss()

                            Toasty.error(
                                this,
                                resource.message ?: "Failed to initiate slitting"
                            ).show()
                        }
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
                        showErrorDialog(this,resource.message ?: "Print failed",)
//                            Toasty.LENGTH_SHORT)

//                        Toasty.error(
//                            this,
//                            resource.message ?: "Print failed",
//                            Toasty.LENGTH_SHORT
//                        ).show()

                        Log.e("PRINT_ERROR", resource.message ?: "")
                    }

                    else -> {}
                }
            }

            slittingWithoutplanvViewModel.coilSplitLiveData.observe(this) { result ->

                when (result) {

                    is Resource.Loading -> progress.show()

                    is Resource.Success -> {
                        progress.dismiss()
                        showPrintDialog()
                        splitBarcode = result.data?.responseObject.toString()

                        Log.d("SPLIT_BARCODE", "Generated Barcode = $splitBarcode")
                        Toasty.success(
                            this,
                            result.data?.responseMessage ?: "Coil split successful"
                        ).show()

                        val splitBarcode = binding.commanInputRow.inputField.text.toString().trim()
                        slittingWithoutplanvViewModel
                            .getStockByBatchOrBarcode(splitBarcode)
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        showErrorDialog(this,result.message ?: "Error")

//                        Toasty.error(this, result.message ?: "Error").show()
                    }
                }
            }
            binding.btncSaves.setOnClickListener {

                if (sourceStockId == 0) {
                    Toasty.warning(this, "Please scan coil first").show()
                    return@setOnClickListener
                }
                val weights = getAllWeights()

                if (weights.isEmpty() || weights.any { it.isBlank() }) {
                    Toasty.warning(this, "Please enter width").show()
                    return@setOnClickListener
                }

                weights.forEach {

                    val w = it.toDoubleOrNull() ?: 0.0

                    if (w > maxAllowedWidth) {

                        Toasty.error(
                            this,
                            "One of the widths is greater than stock width"
                        ).show()

                        return@setOnClickListener
                    }
                }
                val totalWidth = getTotalEnteredWidth()

                if (totalWidth > maxAllowedWidth) {

                    Toasty.error(
                        this,
                        "Total width cannot exceed $maxAllowedWidth mm"
                    ).show()

                    return@setOnClickListener
                }

                val request = InitiateSlittingWithoutPlanRequest(
                    HRSlittingTranId = 0,
                    TenantCode = tenantCode ?: "",
                    HRSlittingPlanId = 0,
                    LocationId = locationId,
                    SourceStockId = sourceStockId,
                    Barcode = scannedBarcode,
                    CompletedBy = "",
                    CompletedDate = "",
                    Status = "",
                    process=selectedProcessName,
                    Remarks = "Slitting without plan",

                    hrSlittingTransactionDetail = buildTransactionDetails(transactionId)
                )

                slittingWithoutplanvViewModel
                    .initiateSlittingWithoutPlan(request)
            }
//            binding.btncClears.setOnClickListener {
//
//                binding.commanInputRow.inputField.text?.clear()
////                binding.layoutBatchDetails.visibility = View.GONE
//                binding.layoutWeightContainer.removeAllViews()
//
//                sourceStockId = 0
//                scannedBarcode = null
//            }


       }
        private fun addWeightRow() {

            val rowView = layoutInflater.inflate(
                R.layout.item_weight_row,
                binding.layoutWeightContainer,
                false
            )

            val etWeight = rowView.findViewById<EditText>(R.id.etWeight)
            val btnEdit = rowView.findViewById<ImageButton>(R.id.btnEdit)
            val btnAddMore = rowView.findViewById<ImageButton>(R.id.btnAddMore)

            // Safety check
            if (etWeight == null || btnEdit == null || btnAddMore == null) {
                Log.e("SLITTING", "View not found in item_weight_row.xml")
                return
            }

            etWeight.isEnabled = true


            etWeight.setOnFocusChangeListener { _, hasFocus ->

                isEnteringWidth = hasFocus

                if (hasFocus) {

                    binding.commanInputRow.inputField.clearFocus()

                }
            }

            btnEdit.visibility = View.GONE
            btnAddMore.visibility = View.VISIBLE

            etWeight.addTextChangedListener {

                val currentValue = it.toString().toDoubleOrNull() ?: 0.0

                // Total including this field
                val totalWidth = getTotalEnteredWidth()

                // Individual check
                if (currentValue > maxAllowedWidth) {

                    Toasty.warning(
                        this,
                        "Width cannot be greater than $maxAllowedWidth mm"
                    ).show()

                    etWeight.setText("")
                    return@addTextChangedListener
                }

                // Total check
                if (totalWidth > maxAllowedWidth) {

                    Toasty.error(
                        this,
                        "Total width cannot be greater than $maxAllowedWidth mm"
                    ).show()

                    etWeight.setText("")
                }
            }

            btnEdit.setOnClickListener {

                etWeight.isEnabled = true

                binding.commanInputRow.inputField.clearFocus()

                etWeight.requestFocus()
                etWeight.setSelection(etWeight.text.length)
            }

            btnAddMore.setOnClickListener {

                val entered = etWeight.text.toString().toDoubleOrNull()

                if (entered == null) {

                    Toasty.warning(this, "Enter valid width").show()
                    return@setOnClickListener
                }

                val totalWidth = getTotalEnteredWidth()

                // Individual check
                if (entered > maxAllowedWidth) {

                    Toasty.warning(
                        this,
                        "Width cannot be greater than $maxAllowedWidth mm"
                    ).show()

                    return@setOnClickListener
                }

                // Total check
                if (totalWidth > maxAllowedWidth) {

                    Toasty.error(
                        this,
                        "Total width exceeded $maxAllowedWidth mm"
                    ).show()

                    return@setOnClickListener
                }

                etWeight.isEnabled = false

                btnAddMore.visibility = View.GONE
                btnEdit.visibility = View.VISIBLE

                addWeightRow()
            }

            binding.layoutWeightContainer.addView(rowView)
        }




        private fun getAllWeights(): List<String> {
            val weights = mutableListOf<String>()
            for (i in 0 until binding.layoutWeightContainer.childCount) {
                val row = binding.layoutWeightContainer.getChildAt(i)
                val etWeight = row.findViewById<EditText>(R.id.etWeight)
                weights.add(etWeight.text.toString())
            }
            return weights
        }
        private fun getTotalEnteredWidth(): Double {

            var total = 0.0

            for (i in 0 until binding.layoutWeightContainer.childCount) {

                val row = binding.layoutWeightContainer.getChildAt(i)
                val etWeight = row.findViewById<EditText>(R.id.etWeight)

                val value = etWeight.text.toString().toDoubleOrNull() ?: 0.0

                total += value
            }

            return total
        }

        private fun buildTransactionDetails(
            hrSlittingTranId: Int
        ): List<HRSlittingTransactionDetailRequest> {

            val details = mutableListOf<HRSlittingTransactionDetailRequest>()
            val weights = getAllWeights()

            weights.forEach { weightStr ->

                if (weightStr.isNotBlank()) {

                    details.add(
                        HRSlittingTransactionDetailRequest(
                            HRSlittingTranDtlId = 0,
                            HRSlittingTranId = hrSlittingTranId,
                            Width = weightStr.toDouble(),

                            WeightLocationId = locationId,

                            IsActive = true,
                            Status = "InProgress"
                        )
                    )
                }
            }

            return details
        }
        @SuppressLint("NewApi")
        private fun showPrintDialog() {

            MaterialAlertDialogBuilder(this)
                .setTitle("Print Label")
                .setMessage("Do you want to print the split coil label?")
                .setPositiveButton("Print") { _, _ ->

                    if (splitBarcode.isNullOrEmpty()) {

                        Toasty.error(
                            this,
                            "Split barcode not found"
                        ).show()

                        return@setPositiveButton
                    }

                    val printRequestList = listOf(
                        PrintLabelBarcodeRequest(
                            barcode = splitBarcode!!,
                            locationId = locationId,
                            createdDate = Utils.getCurrentDateTimeISO(),
                            createdBy = userName ?: ""
                        )
                    )

                    Log.d(
                        "PRINT_REQUEST",
                        "Printing Barcode = $splitBarcode"
                    )

                    printlabelViewModel.printLabelBarcode(printRequestList)
                }
                .setNegativeButton("Skip", null)
                .show()
        }
        private fun showSlitDialog() {

            val dialogView = layoutInflater.inflate(R.layout.dialog_slit_input, null)

//            val etWidth = dialogView.findViewById<EditText>(R.id.etWidth)
            val etWeight = dialogView.findViewById<EditText>(R.id.etWeight)
            val btnSubmit = dialogView.findViewById<MaterialButton>(R.id.btnSubmit)
            val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)

            val dialog = android.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSubmit.setOnClickListener {

//                val width = etWidth.text.toString().toDoubleOrNull()
                val weight = etWeight.text.toString().toDoubleOrNull()

//                if (width == null || weight == null) {
//                    Toasty.warning(this, "Enter valid Width & Weight").show()
//                    return@setOnClickListener
//                }

                if (sourceStockId == 0) {
                    Toasty.warning(this, "Please scan coil first").show()
                    return@setOnClickListener
                }
                val request = weight?.let { it1 ->
                    CoilSplitRequest(
                        StockId = sourceStockId,
                        Weight = it1,

                        Remark = "Slit from app",
                        UserName = userName ?: "",
                        TenantCode = tenantCode ?: ""
                    )
                }

                if (request != null) {
                    slittingWithoutplanvViewModel.coilSplit(request)
                }

                dialog.dismiss()
            }

            dialog.show()
        }
        private fun showPlanningSection() {
            binding.cardBatch.visibility = View.VISIBLE
            binding.layoutTopButtons.visibility = View.VISIBLE

            binding.layoutActionButtons.visibility = View.VISIBLE
        }

        private fun hidePlanningSection() {
            binding.cardBatch.visibility = View.GONE
            binding.layoutTopButtons.visibility = View.GONE
            binding.cardWeight.visibility = View.GONE
            binding.layoutActionButtons.visibility = View.GONE
        }

    }