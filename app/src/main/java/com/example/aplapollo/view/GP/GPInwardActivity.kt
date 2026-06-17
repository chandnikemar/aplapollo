package com.example.aplapollo.view.GP

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.GP.GalvanizingTransactionRequest
import com.example.aplapollo.view.BaseScanActivity
import com.example.aplapollo.viewmodel.GP.GpViewModel
import com.example.aplapollo.viewmodel.GP.GpViewModelFactory
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomInputCodeViewModelfactory
import com.example.aplapollo.viewmodel.bommaster.BomViewModel
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanViewModelfactory
import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanvViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGpinwardBinding
import es.dmoral.toasty.Toasty

class GPInwardActivity : BaseScanActivity() {
    private lateinit var binding: ActivityGpinwardBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var gpViewModel: GpViewModel
    private  lateinit var  picklingViewModel: PicklingViewModel
    private lateinit var slittingWithoutplanvViewModel: SlittingWithoutplanvViewModel
    private lateinit var bomViewModel: BomViewModel

    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var locationId: Int = 0
    private var locationName: String = ""
    private var sourceStockId: Int = 0
    private var scannedBarcode: String? = null
    private  var tenantCode:String?=null
    private var transactionId:Int=0
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    private var selectedBomComponentId: Int = 0
    private var selectedBomOutputId: Int = 0
    private var selectedComponentMaterialCode: String = ""
    private var isEnteringWidth = false
    override fun onBarcodeScanned(barcode: String) {

        if (isEnteringWidth) {

            Log.d(
                "SCAN_DEBUG",
                "Ignored scan while entering weight"
            )

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
                   }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gpinward)
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
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        val viewModelProviderFactoryGp = GpViewModelFactory(application, retrofitInstance)
        gpViewModel = ViewModelProvider(this, viewModelProviderFactoryGp)[GpViewModel::class.java]
        val viewModelProviderFactory = SlittingWithoutplanViewModelfactory(application, retrofitInstance)
        slittingWithoutplanvViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingWithoutplanvViewModel::class.java]
        val viewModelProviderFactoryPickling = PicklingViewModelfactory(application, retrofitInstance)
        picklingViewModel = ViewModelProvider(this, viewModelProviderFactoryPickling)[PicklingViewModel::class.java]
        val viewModelProviderFactoryBom =
            BomInputCodeViewModelfactory(application, retrofitInstance)

        bomViewModel = ViewModelProvider(
            this,
            viewModelProviderFactoryBom
        )[BomViewModel::class.java]
        binding.idLayoutHeader.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed()}
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

        locationId = intent.getIntExtra(Constants.LocationId, 0)
        Log.d("Tanent_Code","Tenant Code= $locationId")
        selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
        binding.idLayoutHeader.tvTitle.text = "$selectedProcessName Planning"
        binding.idLayoutHeader.tvSubtitle.text="Generate production plans from scanned coils"



        binding.editC4.setOnFocusChangeListener { _, hasFocus ->

            isEnteringWidth = hasFocus

            Log.d(
                "WEIGHT_FOCUS",
                "Weight focus = $hasFocus"
            )
        }
        slittingWithoutplanvViewModel.stockByBarcodeLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()
                    binding.commanInputRow.inputField.clearFocus()
                    val data = result.data ?: return@observe
                    Log.d("BARCODE", data.toString())
                    binding.layoutBatchDetails.visibility = View.VISIBLE

                    binding.inCommanBatch.tvItemCode.text =
                        data?.materialCode ?: ""

                    binding.inCommanBatch.tvGrade.text =
                        "${data?.grade ?: "-"}"

                    binding.inCommanBatch.tvSupplierBatchNo.text =
                        "${data?.supplierBatchNo ?: "-"}"

                    binding.inCommanBatch.tvWidth.text =
                        "%.3f".format(data?.width.toString().toDoubleOrNull() ?: 0.0)

                    binding.inCommanBatch.tvThickness.text =
                        "%.2f".format(data?.thickness.toString().toDoubleOrNull() ?: 0.0)
                    binding.inCommanBatch.tvWeight.text =
                        "%.3f".format(data?.weight.toString().toDoubleOrNull() ?: 0.0)
                    sourceStockId = data?.stockId!!
                    scannedBarcode = data?.barcode
                    tenantCode=data?.tenantCode
                    transactionId=data?.transactionId?:0

                }

                is Resource.Error -> {
                    progress.dismiss()
                    Utils.showErrorDialog(this, result.message ?: "Error",)
//                    Toasty.error(
//                        this,
//                        result.message ?: "Invalid barcode",
//                        Toasty.LENGTH_SHORT
//                    ).show()

                }

                else -> {}
            }

        }
        gpViewModel.initiateGpLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    Toast.makeText(
                        this,
                        result.data,
                        Toast.LENGTH_LONG
                    ).show()

                    finish() // optional
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toast.makeText(
                        this,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }
        bomViewModel.bomComponentLiveData.observe(this) { result ->

            when (result) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    val list = result.data ?: emptyList()

                    if (list.isEmpty()) {

                        Toasty.warning(
                            this,
                            "No Output Material Found"
                        ).show()

                        return@observe
                    }

                    val materialList =
                        list.map { "${it.componentCode} - ${it.materialDescription}" }

                        androidx.appcompat.app.AlertDialog.Builder(this)
//                            .setTitle("Select component Material")
                            .setItems(materialList.toTypedArray()) { _, position ->

                                val selectedItem = list[position]

                                selectedBomComponentId =
                                    selectedItem.boMComponentId

                                selectedBomOutputId =
                                    selectedItem.boMOutputId

                                selectedComponentMaterialCode =
                                    selectedItem.componentCode
                                binding.tvOutputMaterial.text =
                                    selectedItem.componentCode
                                binding.tvOutputDesc.text =
                                    selectedItem.materialDescription
                                binding.commanInputRow.inputField.clearFocus()
                            }
                            .show()
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toasty.error(
                        this,
                        result.message ?: "Error",
                        Toasty.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
        binding.layoutOutputMaterial.setOnClickListener {

            val inputMaterial =
                binding.inCommanBatch.tvItemCode.text.toString().trim()

            if (inputMaterial.isEmpty()) {

                Toasty.warning(
                    this,
                    "Please scan material first"
                ).show()

                return@setOnClickListener
            }

            bomViewModel.getBomComponents(inputMaterial)
        }
        binding.commanInputRow.btnSearch.setOnClickListener {

            val barcode = binding.commanInputRow.inputField.text.toString().trim()

            if (barcode.isEmpty()) {
                Toasty.warning(this, "Please scan barcode").show()
                return@setOnClickListener
            }
            slittingWithoutplanvViewModel
                .getStockByBatchOrBarcode(barcode)
        }
        binding.commanInputRow.btnClear.setOnClickListener {

            // Clear input field
            binding.commanInputRow.inputField.setText("")

            // Hide data card
            binding.layoutBatchDetails.visibility = View.GONE

            // Reset values
            scannedBarcode = null
            sourceStockId = 0
            transactionId = 0

            Toasty.info(this, "Input cleared").show()
        }
        binding.btncClears.setOnClickListener {

            // Clear input field also (optional but recommended)
            binding.commanInputRow.inputField.setText("")

            // Clear UI data
            binding.inCommanBatch.tvItemCode.text = ""
            binding.inCommanBatch.tvGrade.text = ""
            binding.inCommanBatch.tvSupplierBatchNo.text = ""
            binding.inCommanBatch.tvWidth.text = ""
            binding.inCommanBatch.tvThickness.text = ""
            binding.inCommanBatch.tvWeight.text = ""

            // Hide card
            binding.layoutBatchDetails.visibility = View.GONE

            // Reset variables
            scannedBarcode = null
            sourceStockId = 0
            transactionId = 0


        }
        binding.btncSaves.setOnClickListener {
            if (locationId == 0) {
                Toasty.warning(this, "Station is missing").show()
                return@setOnClickListener
            }

            if (scannedBarcode == null || transactionId == 0) {

                Toasty.warning(
                    this,
                    "Please scan barcode first"
                ).show()

                return@setOnClickListener
            }
            val zincMaterial = selectedComponentMaterialCode.trim()

            if (zincMaterial.isEmpty()) {

                Toasty.warning(
                    this,
                    "Please select zinc material"
                ).show()

                binding.layoutOutputMaterial.performClick()

                return@setOnClickListener
            }

            val zincWeightText =
                binding.editC4.text.toString().trim()

            if (zincWeightText.isEmpty()) {

                Toasty.warning(
                    this,
                    "Please enter zinc weight"
                ).show()

                return@setOnClickListener
            }

            val zincWeight = zincWeightText.toDoubleOrNull()

            if (zincWeight == null || zincWeight <= 0) {

                Toasty.warning(
                    this,
                    "Enter valid zinc weight"
                ).show()

                return@setOnClickListener
            }


            val request = GalvanizingTransactionRequest(
                galvanizingTranId  = 0,

                locationId = locationId,
                sourceStockId = sourceStockId,
                jobNumber = "",
                isDivided = false,
                status = "InProgress",
                remarks = "Galvanizing Proccess",
                zincMaterialCode = selectedComponentMaterialCode,
                process=selectedProcessName,
                zincWeight =zincWeight
                )

            gpViewModel.initiateGp(request)
            Log.d("GP_POST", "Request Body = $request")
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        if (::progress.isInitialized && progress.isShowing) {
            progress.dismiss()
        }
//        @SuppressLint("MissingInflatedId")
//        fun showSearchableOutputDialog(
//            activity: Activity,
//            title: String = "Select Material",
//            items: List<BoMComponentResponse>,
//            onSelect: (BoMComponentResponse) -> Unit
//        ) {
//
//            val dialog = AlertDialog.Builder(activity).create()
//
//            val view = activity.layoutInflater.inflate(
//                R.layout.dialog_output_material,
//                null
//            )
//
////            val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
//            val recycler = view.findViewById<RecyclerView>(R.id.recyclerOutput)
//            val searchView =
//                view.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchOutput)
//
////            tvTitle.text = title
//
//            recycler.layoutManager = LinearLayoutManager(activity)
//
//            searchView.isIconified = false
//            searchView.clearFocus()
//            searchView.queryHint = "Search component..."
//
//            var filteredList = items.toMutableList()
//
//            lateinit var adapter: GpDialogAdapter
//
//            adapter = GpDialogAdapter(filteredList) { selected ->
//
//                onSelect(selected)
//                dialog.dismiss()
//            }
//
//            recycler.adapter = adapter
//
//            fun applyFilter(query: String) {
//
//                filteredList = items.filter {
//
//                    it.componentCode?.contains(query, true) == true ||
//                            it.materialDescription?.contains(query, true) == true
//
//                }.toMutableList()
//
//                adapter = GpDialogAdapter(filteredList) { selected ->
//
//                    onSelect(selected)
//                    dialog.dismiss()
//                }
//
//                recycler.adapter = adapter
//            }
//
//            searchView.setOnQueryTextListener(
//                object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//
//                    override fun onQueryTextSubmit(query: String?): Boolean {
//                        applyFilter(query.orEmpty())
//                        return true
//                    }
//
//                    override fun onQueryTextChange(newText: String?): Boolean {
//                        applyFilter(newText.orEmpty())
//                        return true
//                    }
//                }
//            )
//
//            dialog.setView(view)
//
//            dialog.show()
//
//            dialog.window?.apply {
//
//                setBackgroundDrawableResource(android.R.color.transparent)
//
//                val width =
//                    (activity.resources.displayMetrics.widthPixels * 0.95).toInt()
//
//                val height =
//                    (activity.resources.displayMetrics.heightPixels * 0.80).toInt()
//
//                setLayout(width, height)
//            }
//        }
    }

}