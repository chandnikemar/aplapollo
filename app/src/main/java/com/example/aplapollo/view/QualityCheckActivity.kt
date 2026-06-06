package com.example.aplapollo.view

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.Utils.showErrorDialog
import com.example.aplapollo.helper.Utils.todayDate
import com.example.aplapollo.helper.ZebraPrinterHelper
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.QCFetchData
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel

import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.aplapollo.viewmodel.qualitycheck.QCViewModel
import com.example.aplapollo.viewmodel.qualitycheck.QcViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityQualityCheckBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import es.dmoral.toasty.Toasty


class QualityCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQualityCheckBinding
    private lateinit var qcviewModel: QCViewModel
    private lateinit var qcPrintLabelViewModel: PrintlabelViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var tenantCode: String
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""

    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var selectedMaterialTypeId: Int = 0
    private var qcStatus: String = ""
    private var scanBuffer = StringBuilder()
    private var lastKeyTime = 0L
    private val SCAN_TIMEOUT = 300L



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_quality_check)
        binding.commanInputRow.inputField.apply {

            requestFocus()

            isFocusable = true
            isFocusableInTouchMode = true

            post {
                requestFocus()

            }
        }
        binding.idLayoutHeader.tvTitle.text = "Quality Check"
        binding.idLayoutHeader.tvSubtitle.text = "Scan Coil and Check the Quality"

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val headerBinding = binding.idLayoutHeader

        headerBinding.printerStatusContainer.visibility = View.VISIBLE
        headerBinding.ivPrinter.setImageResource(R.drawable.printer_white_on)


        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactory = QcViewModelFactory(application, retrofitInstance)
        qcviewModel = ViewModelProvider(this, viewModelProviderFactory)[QCViewModel::class.java]
        val printlabelviewModelProviderFactory=QcprintlabelViewModelFactory(application,retrofitInstance)
        qcPrintLabelViewModel =ViewModelProvider(this,printlabelviewModelProviderFactory)[PrintlabelViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode = userDetail!![SessionManager.Key_tenantCode]?.toString() ?: ""


            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


        }
        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        ) //Focus for Input Filed


//        startService(intent)
        binding.layoutQcContainer.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutBarcodeSection.visibility = View.GONE
        binding.rowRemarkSubmit.visibility = View.GONE

        Log.d("Tanent_Code","Tenant Code= $tenantCode")

        binding.commanInputRow.btnSearch.setOnClickListener {

            val coilNumber = binding.commanInputRow.inputField.text.toString().trim()

            if (coilNumber.isEmpty()) {
                showErrorMessage("Please enter Coil Batch Number")
                return@setOnClickListener
            }

            clearPreviousQCData()

            val request = QCFetchRequest(
                coilBatchNumber = coilNumber,
                tenantCode = tenantCode
            )

            qcviewModel.fetchQCData(request)
        }


        qcviewModel.qcFetchLiveData.observe(this) { response ->
            when (response) {

                is Resource.Loading -> showProgressBar()

                is Resource.Success -> {
                    hideProgressBar()

                    val data = response.data

                    if (data?.statusCode == 200 && data.responseObject != null) {

                        binding.layoutEmpty.visibility = View.GONE
                        binding.layoutQcContainer.visibility = View.VISIBLE

                        setQCDataToUI(data.responseObject)

                        binding.buttonLeft.visibility = View.VISIBLE
                        binding.buttonRight.visibility = View.VISIBLE

                    } else {

                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.layoutQcContainer.visibility = View.GONE

                        showErrorMessage("Invalid Coil Batch Number")
                    }

                }


                is Resource.Error -> {
                    hideProgressBar()
                    showErrorMessage(response.message ?: "Unknown Error")
                }

                else -> {}
            }
        }

        qcviewModel.barcodeLiveData.observe(this) { response ->
            when (response) {

                is Resource.Loading -> {
                    showProgressBar()

                    // ✅ Disable both buttons during API call
                    binding.buttonLeft.isEnabled = false
                    binding.buttonRight.isEnabled = false
                    binding.buttonLeft.alpha = 0.5f
                    binding.buttonRight.alpha = 0.5f
                }

                is Resource.Success -> {
                    hideProgressBar()

                    val barcodeValue = response.data?.responseMessage
                    if (!barcodeValue.isNullOrEmpty()) {

                        generateBarcode(barcodeValue)

                        binding.layoutBarcodeSection.visibility = View.VISIBLE
                        binding.buttonPrintLabel.visibility = View.VISIBLE

                        binding.buttonLeft.visibility = View.GONE
                        binding.buttonRight.visibility = View.GONE

                        binding.rowRemarkSubmit.visibility = View.GONE
                        binding.buttonLeft.alpha = 0.5f
                        binding.buttonRight.alpha = 0.5f

                        showSubmitButton()

                    } else {

                        resetApproveRejectButtons()
                        showErrorMessage("Failed to get barcode")
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()


                    resetApproveRejectButtons()

                    showErrorMessage(response.message ?: "Unknown Error")
                }

                else -> {}
            }
        }

        qcviewModel.qcStatusLiveData.observe(this) { response ->

            when (response) {

                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    hideProgressBar()

                    val barcode =
                        response.data?.responseObject   // ✅ barcode comes here

                    if (!barcode.isNullOrEmpty()) {

                        Log.d("QC_BARCODE", "Barcode from Submit API = $barcode")

                        // ✅ Generate barcode
                        generateBarcode(barcode)

                        // ✅ Show print button

                        binding.buttonPrintLabel.visibility = View.VISIBLE
                        binding.buttonRight.visibility = View.GONE
                        binding.buttonLeft.visibility = View.GONE

                    } else {
                        Toasty.success(
                            this,
                            response.message ?: "QC Status submitted successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                        goToHome()
                    }
                }

                is Resource.Error -> {

                    hideProgressBar()

                    // ✅ Get barcode from API response
                    val barcode = response.data?.responseObject

                    // ✅ Get proper error message
                    val errorMessage =
                        response.data?.errorMessage
                            ?: response.message
                            ?: "Something went wrong"

                    // ✅ 409 Already Exists Case
                    if (!barcode.isNullOrEmpty()) {

                        Log.d("QC_BARCODE", "Existing Barcode = $barcode")

                        // Show barcode image
                        generateBarcode(barcode)

                        // Show barcode layout
                        binding.layoutBarcodeSection.visibility = View.VISIBLE
                        binding.barcodeContainer.visibility = View.VISIBLE

                        // Show ONLY Reprint button
//                        binding.btnReprint.visibility = View.VISIBLE
                        binding.buttonPrintLabel.visibility = View.GONE

                        // Hide approve/reject
                        binding.buttonLeft.visibility = View.GONE
                        binding.buttonRight.visibility = View.GONE

                        // Hide remark layout
                        binding.rowRemarkSubmit.visibility = View.GONE

                        // Set barcode text
                        binding.barcodeText.text = barcode

                        // Show API message
                        showErrorDialog(this,errorMessage)

                    } else {

                        // Real error
                        binding.layoutBarcodeSection.visibility = View.GONE

                        showErrorDialog(this,errorMessage)
                    }
                }

                else -> {}
            }
        }

        qcPrintLabelViewModel.qcPrintMutableLiveData.observe(this) { result ->

                when (result) {

                    is Resource.Loading -> {
                        Log.d("QC_PRINT", "API call loading")
                    }

                    is Resource.Success -> {

                        val zpl = result.data?.responseObject
                        Log.d("QC_PRINT", "ZPL = $zpl")

                        if (zpl.isNullOrEmpty()) {
                            Toast.makeText(this, "ZPL not received", Toast.LENGTH_SHORT).show()
                            return@observe
                        }

                        // ✅ Get printer MAC (use ONLY ONE source)
                        val printerMac = Utils.getSharedPrefs(this,Constants.KEY_PRINTER_MAC)
                        Log.d("QC_PRINT", "Printer MAC = $printerMac")

                        if (printerMac.isNullOrEmpty()) {
                            Toast.makeText(this, "Printer not configured", Toast.LENGTH_SHORT).show()
                            return@observe
                        }

                        // ✅ PRINT VIA FOREGROUND SERVICE
                        ZebraPrinterHelper.printViaService(
                            context = this,
                            mac = printerMac,
                            zpl = zpl
                        )

                        Toast.makeText(this, "Printing started", Toast.LENGTH_SHORT).show()
                        goToHome()
                    }

                    is Resource.Error -> {
                        Log.e("QC_PRINT", "API error: ${result.message}")

                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }


        binding.buttonRight.setOnClickListener {
            qcStatus = "Accepted"   // or "Accepted" as per API

            // ✅ Disable Reject button (NOT Approve)
            binding.buttonLeft.isEnabled = true
            binding.buttonLeft.alpha = 0.5f

            // Keep Approve enabled (optional: show loading)
            binding.buttonRight.isEnabled = true
            binding.buttonRight.alpha = 1.0f

            // Hide remark row
            binding.rowRemarkSubmit.visibility = View.GONE

            val remarkText = binding.etRemark.text.toString().trim()

            if (qcStatus == "Rejected" && remarkText.isEmpty()) {
                showErrorMessage("Please enter remark for rejection")
                return@setOnClickListener
            }

            val request = QCStatusSubmissionRequest(
                qcId = 0,
                tenantCode=tenantCode,
                materialTypeId = 0,
                materialCode= binding.column2RowMaterialCode.text.toString(),
                barcode = "",
                supplierName = binding.column2Row2.text.toString(),
                supplierBatchNo = binding.column2Row1.text.toString(),
                grade = binding.column2Row4.text.toString().trim().takeIf { it.isNotEmpty() && it.lowercase() != "null" }
                    ?: "A",
                netWeight = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                thickness = binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                length = 0.0,
                width = binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                grnNo = binding.column2Row8.text.toString(),
                grnDate = binding.column2Row9.text.toString(),
                status = qcStatus,
                remarks = remarkText,
                CreatedBy = ""
            )

            qcviewModel.submitQCStatus(  request)
        }

        binding.buttonLeft.setOnClickListener {
            qcStatus = "Rejected"

            // ✅ Disable Approve button
            binding.buttonRight.isEnabled = false
            binding.buttonRight.alpha = 0.5f

            // Keep Reject enabled
            binding.buttonLeft.isEnabled = true
            binding.buttonLeft.alpha = 1.0f

            // Show Remark Field
            binding.rowRemarkSubmit.visibility = View.VISIBLE

            // Hide barcode row
            binding.barcodeContainer.visibility = View.GONE

            // Hide normal submit button
//            binding.btnReprint.visibility = View.GONE
        }

        binding.commanInputRow.btnClear.setOnClickListener {
            clearPreviousQCData()
            binding.commanInputRow.inputField.text?.clear()

        }
        binding.buttonASubmit.setOnClickListener {

            val remarkText = binding.etRemark.text.toString().trim()

            if (remarkText.isEmpty()) {
                showErrorMessage("Remark is mandatory for rejection")
                return@setOnClickListener
            }

            val request = QCStatusSubmissionRequest(
                qcId = 0,
                tenantCode = "",
                materialTypeId = selectedMaterialTypeId,
                materialCode = binding.column2RowMaterialCode.text.toString(),
                barcode = "", // No barcode for reject
                supplierName = binding.column2Row2.text.toString(),
                supplierBatchNo = binding.column2Row1.text.toString(),
                grade  = binding.column2Row4.text.toString().trim().takeIf { it.isNotEmpty() && it.lowercase() != "null" }
                    ?: "A",
                netWeight = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                thickness = binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                length=0.0,
                width = binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                grnNo = binding.column2Row8.text.toString(),
                grnDate = binding.column2Row9.text.toString(),
                status = "Rejected",
                remarks = remarkText,
                CreatedBy = userName ?: ""
            )
            binding.buttonRight.visibility = View.GONE
            binding.buttonLeft.visibility=View.GONE
            binding.buttonPrintLabel.visibility=View.GONE
            qcviewModel.submitQCStatus(  request)

        }

//        binding.btnReprint.setOnClickListener {
//            val barcode = binding.barcodeText.text.toString()
//
//            val request = PrintLabelRequest(
//                SupplierName  =  binding.column2Row1.text.toString(),
//                BarCode  = binding.barcodeText.text.toString(),
//                SupplierBatchNo  = binding.column2Row1.text.toString(),
//                MaterialCode  =  binding.column2RowMaterialCode.text.toString(),
//                Grade  = binding.column2Row4.text.toString().trim().takeIf { it.isNotEmpty() && it.lowercase() != "null" }
//                    ?: "A",
//                Thickness  =  binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
//                Width  =  binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
//                GRNNumber  = binding.column2Row8.text.toString(),
//                GRNDate  = binding.column2Row9.text.toString(),
//                NetWeight  = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
//                CreatedBy = "",
//                CreatedDate = "2025-12-15T15:06:16"
//            )
//            Log.d("QC_PRINT", "Print button clicked")
//            Log.d("QC_PRINT", "Request = $request")
//
//            qcPrintLabelViewModel.printQcLabel(  request)
//        }


        binding.buttonPrintLabel.setOnClickListener {
            val barcode = binding.barcodeText.text.toString()

            val request = PrintLabelRequest(
                SupplierName  =  binding.column2Row2.text.toString(),
                BarCode  = binding.barcodeText.text.toString(),
                SupplierBatchNo  = binding.column2Row1.text.toString(),
                MaterialCode  =  binding.column2RowMaterialCode.text.toString(),
                Grade  = binding.column2Row4.text.toString().trim().takeIf { it.isNotEmpty() && it.lowercase() != "null" }
                    ?: "A",
                Thickness  =  binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                Width  =  binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                GRNNumber  = binding.column2Row8.text.toString(),
                GRNDate  = binding.column2Row9.text.toString(),
                NetWeight  = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                CreatedBy = userName ?: "",
                CreatedDate = todayDate
            )
            Log.d("QC_PRINT", "Print button clicked")
            Log.d("QC_PRINT", "Request = $request")

            qcPrintLabelViewModel.printQcLabel( request)
//            ZebraPrinterHelper.printZpl(this,request) { success, message ->
//                runOnUiThread {
//                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                }
//            }

        }


    }
    fun updatePrinterStatus(isConnected: Boolean) {

        val header = binding.idLayoutHeader

        header.printerStatusContainer.visibility = View.VISIBLE

        if (isConnected) {
            header.viewPrinterStatus
                .setBackgroundResource(R.drawable.bg_status_green)
        } else {
            header.viewPrinterStatus
                .setBackgroundResource(R.drawable.bg_status_red)
        }
    }

    private fun resetApproveRejectButtons() {
        binding.buttonLeft.isEnabled = true
        binding.buttonRight.isEnabled = true
        binding.buttonLeft.alpha = 1.0f
        binding.buttonRight.alpha = 1.0f
    }
    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun clearPreviousQCData() {

        binding.layoutQcContainer.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        // Clear barcode
        binding.barcodeContainer.visibility = View.GONE
        binding.barcodeImage.setImageBitmap(null)
        binding.barcodeText.text = ""

        // Clear remarks
        binding.etRemark.text.clear()
        binding.rowRemarkSubmit.visibility = View.GONE

        // Clear output fields
        binding.column2Row1.text = "--"
        binding.column2Row2.text = "--"
        binding.column2Row3.text = "--"
        binding.column2Row4.text = "--"
        binding.column2Row5.text = "--"
        binding.column2Row6.text = "--"

        binding.column2Row8.text = "--"
        binding.column2Row9.text = "--"
        binding.column2RowMaterialCode.text = "--"

        // Hide buttons
//        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
        binding.buttonLeft.visibility = View.GONE
        binding.buttonRight.visibility = View.GONE

        binding.layoutBarcodeSection.visibility = View.GONE
        binding.barcodeContainer.visibility = View.GONE
        binding.barcodeImage.setImageBitmap(null)
        binding.barcodeText.text = ""

        // Reset state
        selectedMaterialTypeId = 0
        qcStatus = ""
        binding.buttonLeft.isEnabled = true
        binding.buttonRight.isEnabled = true
        binding.buttonLeft.alpha = 1.0f
        binding.buttonRight.alpha = 1.0f
    }


    private fun generateBarcode(data: String) {
        try {

            val bitMatrix: BitMatrix =
                MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.CODE_128,
                    600,
                    200
                )

            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)

            binding.barcodeImage.visibility = View.VISIBLE
            binding.barcodeImage.setImageBitmap(bitmap)

            binding.barcodeText.text = data

            binding.layoutBarcodeSection.visibility = View.VISIBLE
            binding.barcodeContainer.visibility = View.VISIBLE

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showSubmitButton() {
//        binding.btnReprint.visibility = View.VISIBLE
        binding.buttonPrintLabel.visibility = View.GONE


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setQCDataToUI(data: QCFetchData?) {
        if (data == null) {
            showErrorMessage("No QC data found")
            return
        }
        binding.barcodeImage.visibility = View.GONE
        binding.rowRemarkSubmit.visibility = View.GONE
//        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
        binding.barcodeText.text = ""
        binding.barcodeImage.setImageBitmap(null)
        binding.apply {
            barcodeImage.visibility = View.VISIBLE
            column2Row2.text = data.supplierName ?: "--"
            column2Row1.text = data.supplierBatchNo ?: "--"
            column2Row4.text = data.grade ?: "--"
            column2RowMaterialCode.text = data.materialCode ?: "--"
            column2Row5.text = data.thickness?.toString() ?: "--"
            column2Row6.text = data.width?.toString() ?: "--"

            column2Row3.text = data.netWeightKg?.toString() ?: "--"
            column2Row8.text = data.grnNumber?.toString() ?: "--"
            column2Row9.text = data.grnDate?.substringBefore("T") ?: "--"
        }

        binding.buttonRight.isEnabled = true
        binding.buttonRight.alpha = 1.0f



    }

    private fun showErrorMessage(message: String) {
        Toasty.warning(this@QualityCheckActivity, message, Toasty.LENGTH_SHORT).show()
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {

        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastKeyTime > SCAN_TIMEOUT) {
                scanBuffer.clear()
            }
            lastKeyTime = now
            val char = event.unicodeChar.toChar()
            if (char.code > 0) {
                scanBuffer.append(char)
            }
            if (event.keyCode == android.view.KeyEvent.KEYCODE_ENTER) {
                val scannedCode = scanBuffer.toString().trim()
                scanBuffer.clear()

                if (scannedCode.isNotEmpty()) {
                    handleScannedCoil(scannedCode)
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
    private fun clearUIData() {

        binding.layoutBarcodeSection.visibility = View.GONE
        binding.barcodeImage.setImageBitmap(null)
        binding.barcodeText.text = ""

        binding.etRemark.text.clear()
        binding.rowRemarkSubmit.visibility = View.GONE

        binding.column2Row1.text = "--"
        binding.column2Row2.text = "--"
        binding.column2Row3.text = "--"
        binding.column2Row4.text = "--"
        binding.column2Row5.text = "--"
        binding.column2Row6.text = "--"
        binding.column2Row8.text = "--"
        binding.column2Row9.text = "--"
        binding.column2RowMaterialCode.text = "--"

//        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
    }
    private fun handleScannedCoil(coilNumber: String) {

        Log.d("QC_SCANNER", "Scanned = $coilNumber")
        clearUIData()
        binding.commanInputRow.inputField.setText(coilNumber)
        Log.d("QC_INPUT", "Input text = ${binding.commanInputRow.inputField.text}")
        val request = QCFetchRequest(
            RequestId = "1",
            coilBatchNumber = coilNumber
        )

        qcviewModel.fetchQCData( request)

        binding.buttonLeft.visibility = View.VISIBLE
        binding.buttonRight.visibility = View.VISIBLE
    }


}