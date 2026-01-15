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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionExpiredEvent
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.ZebraPrinterHelper
import com.example.aplapollo.model.QualityCheck.PrintLabelRequest
import com.example.aplapollo.model.QualityCheck.QCFetchData
import com.example.aplapollo.model.QualityCheck.QCFetchRequest
import com.example.aplapollo.model.QualityCheck.QCStatusSubmissionRequest
import com.example.aplapollo.repository.APLRepository
import com.example.aplapollo.viewmodel.printlabel.QcPrintlabelViewModel
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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


class QualityCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQualityCheckBinding
    private lateinit var qcviewModel: QCViewModel
    private lateinit var qcPrintLabelViewModel:QcPrintlabelViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var selectedMaterialTypeId: Int = 0
    private var qcStatus: String = ""



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_quality_check)

        binding.idLayoutHeader.tvTitle.text = "Quality Check"

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
            supportActionBar?.hide()
            progress = ProgressDialog(this)
            progress.setMessage("Please Wait...")
            val aplRepository = APLRepository()
            val viewModelProviderFactory = QcViewModelFactory(application, aplRepository)
            qcviewModel = ViewModelProvider(this, viewModelProviderFactory)[QCViewModel::class.java]
        val printlabelviewModelProviderFactory=QcprintlabelViewModelFactory(application,aplRepository)
        qcPrintLabelViewModel =ViewModelProvider(this,printlabelviewModelProviderFactory)[QcPrintlabelViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode= userDetail!!["defaultTenantCode"].toString()

            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
            if (shouldLogout == true) {
                SessionExpiredEvent.logoutLiveData.value = false
                showLogoutPopup()
            }
        }


//        startService(intent)
        binding.buttonLeft.visibility = View.GONE
        binding.buttonRight.visibility = View.GONE
        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE

        binding.commanInputRow.buttonGetDetail.setOnClickListener {

            val coilNumber = binding.commanInputRow.inputField.text.toString().trim()
            clearPreviousQCData()
            if (coilNumber.isEmpty()) {
                showErrorMessage("Please enter Coil Batch Number")
                return@setOnClickListener
            }

            val request = QCFetchRequest(
                RequestId = "1",
                coilBatchNumber = coilNumber
            )

            val qcfectUrl = baseUrl + Constants.Get_GRNData
            Log.d("QCDATAURL", "QC URL: $qcfectUrl")

            qcviewModel.fetchQCData(baseUrl, request)
            binding.buttonLeft.visibility = View.VISIBLE
            binding.buttonRight.visibility = View.VISIBLE

        }

        qcviewModel.qcFetchLiveData.observe(this) { response ->
            when (response) {

                is Resource.Loading -> showProgressBar()

                is Resource.Success -> {
                    hideProgressBar()

                    val data = response.data

                    if (data?.statusCode == 200 && data.responseObject != null) {

                        setQCDataToUI(data.responseObject)

                        // ✅ Show Approve / Reject only on valid data
                        binding.buttonLeft.visibility = View.VISIBLE
                        binding.buttonRight.visibility = View.VISIBLE

                    } else {
                        // ❌ Invalid batch number
                        clearPreviousQCData()
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

                        // ✅ Keep both disabled after barcode generation
                        binding.buttonLeft.isEnabled = false
                        binding.buttonRight.isEnabled = false
                        binding.buttonLeft.alpha = 0.5f
                        binding.buttonRight.alpha = 0.5f

                        showSubmitButton()

                    } else {
                        // ❌ API success but no barcode
                        resetApproveRejectButtons()
                        showErrorMessage("Failed to get barcode")
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()

                    // ❌ API error → re-enable buttons
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
                            "QC Status submitted successfully",
                            Toasty.LENGTH_SHORT
                        ).show()
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()

                    val barcode = response.data?.responseObject

                    if (!barcode.isNullOrEmpty()) {

                        // ✅ 409 case → barcode exists
                        generateBarcode(barcode)

                        binding.btnReprint.visibility = View.VISIBLE
                        binding.buttonLeft.visibility = View.GONE
                        binding.buttonRight.visibility = View.GONE
                        binding.buttonPrintLabel.visibility = View.GONE

                        Toasty.info(
                            this,
                            response.message ?: "Barcode already exists",
                            Toasty.LENGTH_SHORT
                        ).show()

                    } else {
//                        response.data?.responseObject?.let {
//                            generateBarcode(it)
//                            binding.btnReprint.visibility = View.VISIBLE
//                        }


                        binding.barcodeContainer.visibility=View.VISIBLE

                        binding.btnReprint.visibility = View.VISIBLE
                        // ❌ real error → NO barcode
                        showErrorMessage(response.message ?: "Something went wrong")
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
//            binding.buttonLeft.isEnabled = false
//            binding.buttonLeft.alpha = 0.5f





            val request = QCStatusSubmissionRequest(
                qcId = 0,
                tenantCode="",
                materialTypeId = 0,
                materialCode= binding.column2RowMaterialCode.text.toString(),
                barcode = "",
                supplierName = binding.column2Row2.text.toString(),
                supplierBatchNo = binding.column2Row1.text.toString(),
                grade = binding.column2Row4.text.toString(),
                netWeight = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                thickness = binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                length = binding.column2Row7.text.toString().toDoubleOrNull() ?: 0.0,
                width = binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                grnNo = binding.column2Row8.text.toString(),
                grnDate = binding.column2Row9.text.toString(),
                status = qcStatus,
                remarks = remarkText,
                CreatedBy = ""
            )

            qcviewModel.submitQCStatus(baseUrl,  request)
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
            binding.rowBarcode.visibility = View.GONE

            // Hide normal submit button
            binding.btnReprint.visibility = View.GONE
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
                grade = binding.column2Row4.text.toString(),
                netWeight = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                thickness = binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                length = binding.column2Row7.text.toString().toDoubleOrNull() ?: 0.0,
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
            qcviewModel.submitQCStatus(baseUrl,  request)
        }

        binding.btnReprint.setOnClickListener {
            val barcode = binding.barcodeText.text.toString()

            val request = PrintLabelRequest(
                SupplierName  =  binding.column2Row1.text.toString(),
                BarCode  = binding.barcodeText.text.toString(),
                SupplierBatchNo  = binding.column2Row1.text.toString(),
                MaterialCode  =  binding.column2RowMaterialCode.text.toString(),
                Grade  = binding.column2Row4.text.toString(),
                Thickness  =  binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                Width  =  binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                GRNNumber  = binding.column2Row8.text.toString(),
                GRNDate  = binding.column2Row9.text.toString(),
                NetWeight  = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                CreatedBy = "",
                CreatedDate = "2025-12-15T15:06:16"
            )
            Log.d("QC_PRINT", "Print button clicked")
            Log.d("QC_PRINT", "Request = $request")

            qcPrintLabelViewModel.printQcLabel(baseUrl,  request)
        }


        binding.buttonPrintLabel.setOnClickListener {
            val barcode = binding.barcodeText.text.toString()

            val request = PrintLabelRequest(
                SupplierName  =  binding.column2Row1.text.toString(),
                BarCode  = binding.barcodeText.text.toString(),
                SupplierBatchNo  = binding.column2Row1.text.toString(),
                MaterialCode  =  binding.column2RowMaterialCode.text.toString(),
                Grade  = binding.column2Row4.text.toString(),
                Thickness  =  binding.column2Row5.text.toString().toDoubleOrNull() ?: 0.0,
                Width  =  binding.column2Row6.text.toString().toDoubleOrNull() ?: 0.0,
                GRNNumber  = binding.column2Row8.text.toString(),
                GRNDate  = binding.column2Row9.text.toString(),
                NetWeight  = binding.column2Row3.text.toString().toDoubleOrNull() ?: 0.0,
                CreatedBy = "",
                CreatedDate = "2025-12-15T15:06:16"
            )
            Log.d("QC_PRINT", "Print button clicked")
            Log.d("QC_PRINT", "Request = $request")

            qcPrintLabelViewModel.printQcLabel(baseUrl,  request)
//            ZebraPrinterHelper.printZpl(this,request) { success, message ->
//                runOnUiThread {
//                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                }
//            }

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
        // Clear barcode
        binding.rowBarcode.visibility = View.GONE
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
        binding.column2Row7.text = "--"
        binding.column2Row8.text = "--"
        binding.column2Row9.text = "--"
        binding.column2RowMaterialCode.text = "--"

        // Hide buttons
        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
        binding.buttonLeft.visibility = View.GONE
        binding.buttonRight.visibility = View.GONE

        binding.rowBarcode.visibility = View.GONE
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
                MultiFormatWriter().encode(data, BarcodeFormat.CODE_128, 600, 200)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            binding.barcodeImage.setImageBitmap(bitmap)
            binding.barcodeText.text = data

            binding.rowBarcode.visibility = View.VISIBLE
            binding.barcodeContainer.visibility = View.VISIBLE

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun showSubmitButton() {
        binding.btnReprint.visibility = View.VISIBLE
        binding.buttonPrintLabel.visibility = View.GONE


    }

    private fun hideAllButtons() {
        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setQCDataToUI(data: QCFetchData?) {
        if (data == null) {
            showErrorMessage("No QC data found")
            return
        }
        binding.rowBarcode.visibility = View.GONE
        binding.rowRemarkSubmit.visibility = View.GONE
        binding.btnReprint.visibility = View.GONE
        binding.buttonPrintLabel.visibility = View.GONE
        binding.barcodeText.text = ""
        binding.barcodeImage.setImageBitmap(null)
        binding.apply {
            rowBarcode.visibility = View.VISIBLE
            column2Row2.text = data.supplierName ?: "--"
            column2Row1.text = data.supplierBatchNo ?: "--"
            column2Row4.text = data.grade ?: "--"
            column2RowMaterialCode.text = data.materialCode ?: "--"
            column2Row5.text = data.thickness?.toString() ?: "--"
            column2Row6.text = data.width?.toString() ?: "--"
            column2Row7.text = data.length?.toString() ?: "--"
            column2Row3.text = data.netWeightKg?.toString() ?: "--"
            column2Row8.text = data.grnNumber?.toString() ?: "--"
            column2Row9.text = formatGrnDate(data.grnDate) ?: "--"
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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatGrnDate(dateString: String?): String {
        return try {
            if (dateString.isNullOrEmpty()) return "--"

            val parsed = OffsetDateTime.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            parsed.format(formatter)

        } catch (e: Exception) {
            Log.e("DATE_FORMAT", "Invalid date: $dateString", e)
            "--"
        }
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
        session.logoutUser()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    }