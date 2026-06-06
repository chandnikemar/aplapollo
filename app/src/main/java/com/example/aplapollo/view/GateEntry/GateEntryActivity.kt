            package com.example.aplapollo.view.GateEntry

            import TransporterAutoAdapter
            import android.app.ProgressDialog
            import android.os.Bundle
            import android.text.Editable
            import androidx.appcompat.app.AppCompatActivity
            import androidx.databinding.DataBindingUtil
            import androidx.lifecycle.ViewModelProvider
            import com.example.aplapollo.api.RetrofitInstance
            import com.example.aplapollo.helper.Resource
            import com.example.aplapollo.helper.Utils
            import com.example.aplapollo.model.GateEntry.GateTransactionRequest
            import com.example.aplapollo.model.GateEntry.TransporterResponse
            import com.example.aplapollo.viewmodel.gateentry.GateEntryViewModelFactory
            import com.example.aplapollo.viewmodel.gateentry.GateTransactionViewModel
            import com.example.apolloapl.R
            import com.example.apolloapl.databinding.ActivityGateEntryBinding
            import es.dmoral.toasty.Toasty
            import kotlinx.coroutines.Job


            class GateEntryActivity : AppCompatActivity() {

                private lateinit var binding: ActivityGateEntryBinding
                private lateinit var gateTransactionViewModel: GateTransactionViewModel
                private lateinit var progress: ProgressDialog

                private val coilList = mutableListOf<String>()
                private var transporterAdapter:
                        TransporterAutoAdapter? = null
                private var gateEntryNo: String = ""   // ✅ IMPORTANT
                private var transactionId: Int = 0
                private var searchJob: Job? = null
                private var isItemSelected = false
                private var shouldShowDropdown = true
                private var fullList: List<TransporterResponse> = emptyList()
                private var transporterList: List<TransporterResponse> = emptyList()
                private var transporterCode: String = ""
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)

                    binding = DataBindingUtil.setContentView(this, R.layout.activity_gate_entry)

                    binding.idLayoutHeader.tvTitle.text = "Create Gate Entry"
                    binding.idLayoutHeader.tvSubtitle.text="Generate a gate entry"
                    binding.idLayoutHeader.ivBack.setOnClickListener {
                        onBackPressedDispatcher.onBackPressed()
                    }

                    supportActionBar?.hide()

                    progress = ProgressDialog(this)
                    progress.setMessage("Please Wait...")

                    // ✅ ViewModel
                    val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
                    val factory = GateEntryViewModelFactory(application, retrofitInstance)

                    gateTransactionViewModel =
                        ViewModelProvider(this, factory)[GateTransactionViewModel::class.java]
    // ================= ADAPTER INIT =================

                    // ================= ADAPTER =================

                    transporterAdapter =
                        TransporterAutoAdapter(
                            this,
                            emptyList()
                        )

                    binding.etTransporterCode.setAdapter(
                        transporterAdapter
                    )


                    binding.etTransporterCode.threshold = 1
                    binding.etTransporterCode.addTextChangedListener(

                        object : android.text.TextWatcher {

                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {

                                if (isItemSelected) {

                                    isItemSelected = false
                                    return
                                }

                                val query =
                                    s.toString().trim()

                                if (query.isNotEmpty()) {

                                    shouldShowDropdown = true

                                    gateTransactionViewModel
                                        .getTransporterList()

                                } else {

                                    shouldShowDropdown = false

                                    binding.etTransporterCode
                                        .dismissDropDown()
                                }
                            }

                            override fun afterTextChanged(
                                s: Editable?
                            ) {
                            }
                        }
                    )
    // ================= FOCUS =================

                    binding.etTransporterCode.setOnFocusChangeListener { _, hasFocus ->

                        if (hasFocus) {

                            if (transporterAdapter!!.count > 0) {

                                binding.etTransporterCode.showDropDown()
                            }
                        }
                    }



                    // ================= ITEM CLICK =================

                    binding.etTransporterCode.setOnItemClickListener {
                            parent,
                            _,
                            position,
                            _ ->

                        isItemSelected = true
                        shouldShowDropdown = false

                        val item =
                            parent.getItemAtPosition(position)
                                    as TransporterResponse

                        // ✅ SAVE TRANSPORTER CODE
                        transporterCode = item.transporterCode ?: ""

                        binding.etTransporterCode.setText(
                            item.transporterCode,
                            false
                        )

                        binding.etTransporterName.setText(
                            item.transporterName
                        )

                        binding.etTransporterCode.dismissDropDown()

                        binding.etTransporterCode.clearFocus()

                        val imm =
                            getSystemService(
                                INPUT_METHOD_SERVICE
                            ) as android.view.inputmethod.InputMethodManager

                        imm.hideSoftInputFromWindow(
                            binding.etTransporterCode.windowToken,
                            0
                        )
                    }
                    gateTransactionViewModel.gateEntryLiveData.observe(this) { response ->

                        when (response) {

                            is Resource.Loading -> progress.show()

                            is Resource.Success -> {
                                progress.dismiss()

                                val data = response.data?.responseObject

                                if (data != null) {
                                    gateEntryNo = data.gateEntryNo   // ✅ store
                                    transactionId = data.gateTransactionId


                                    Toasty.success(
                                        this,
                                        response.data.responseMessage ?: "Success"
                                    ).show()
                                    finish()
                                }
                            }

                            is Resource.Error -> {
                                progress.dismiss()
                                val errorMsg = response.message ?: "Something went wrong"

                               Utils.showErrorDialog(this,errorMsg)
                            }

                            else -> {}
                        }
                    }

                    gateTransactionViewModel.transporterListLiveData.observe(this) { resource ->

                        when (resource) {

                            is Resource.Loading -> {
                                // show loader if needed
                            }

                            is Resource.Success -> {

                                val list =
                                    resource.data ?: emptyList()


                                transporterAdapter!!.updateData(list)


                                transporterAdapter!!.filter.filter(
                                    binding.etTransporterCode.text.toString()
                                )

                                if (
                                    list.isNotEmpty() &&
                                    shouldShowDropdown &&
                                    binding.etTransporterCode.hasFocus()
                                ) {

                                    binding.etTransporterCode.showDropDown()

                                } else {

                                    binding.etTransporterCode.dismissDropDown()
                                }
                            }

    //                        is Resource.Error -> {
    //                            Toasty.error(this, resource.message ?: "Error").show()
    //                        }
                            is Resource.Error -> {    val errorMsg = resource.message ?: "Something went wrong"

                            Utils.showErrorDialog(this,errorMsg)}

                            else -> {}
                        }
                    }
                    binding.btnSubmit.setOnClickListener {

                        val transporterCodeText =
                            binding.etTransporterCode.text.toString().trim()

                        val transporterName =
                            binding.etTransporterName.text.toString().trim()

                        val vehicleNo =
                            binding.etVehicleNo.text.toString().trim()

                        val lrNo =
                            binding.etLrNo.text.toString().trim()



                        when {

                            transporterCodeText.isEmpty() -> {

                                binding.etTransporterCode.error =
                                    "Select Transporter Code"

                                binding.etTransporterCode.requestFocus()
                            }

                            transporterName.isEmpty() -> {

                                binding.etTransporterName.error =
                                    "Enter Transporter Name"

                                binding.etTransporterName.requestFocus()
                            }

                            vehicleNo.isEmpty() -> {

                                binding.etVehicleNo.error =
                                    "Enter Vehicle Number"

                                binding.etVehicleNo.requestFocus()
                            }

                            vehicleNo.length < 6 -> {

                                binding.etVehicleNo.error =
                                    "Enter Valid Vehicle Number"

                                binding.etVehicleNo.requestFocus()
                            }

                            lrNo.isEmpty() -> {

                                binding.etLrNo.error =
                                    "Enter LR Number"

                                binding.etLrNo.requestFocus()
                            }

                            transporterCode.isEmpty() -> {

                                Utils.showErrorDialog(
                                    this,
                                    "Please select transporter from list"
                                )
                            }

                            else -> {

                                // Clear Errors
                                binding.etTransporterCode.error = null
                                binding.etTransporterName.error = null
                                binding.etVehicleNo.error = null
                                binding.etLrNo.error = null

                                val request = GateTransactionRequest(
                                    GateTransactionId = transactionId,
                                    LocationId = null,
                                    GateEntryType = "Inward",
                                    VehicleNumber = vehicleNo,
                                    TransporterName = transporterName,
                                    TransporterNo = transporterCode,
                                    LRNumber = lrNo,
                                    GateEntryNo = null
                                )

                                gateTransactionViewModel.createGateEntry(request)
                            }
                        }
                    }


                    }





              }
