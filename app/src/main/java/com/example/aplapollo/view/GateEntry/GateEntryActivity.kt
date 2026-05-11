        package com.example.aplapollo.view.GateEntry

        import TransporterAutoAdapter
        import android.app.Dialog
        import android.app.ProgressDialog
        import android.os.Bundle
        import android.text.Editable
        import android.text.TextWatcher
        import androidx.appcompat.app.AlertDialog
        import androidx.appcompat.app.AppCompatActivity
        import androidx.databinding.DataBindingUtil
        import androidx.lifecycle.ViewModelProvider
        import androidx.lifecycle.lifecycleScope
        import com.example.aplapollo.adapter.GateEntry.TransporterDialogAdapter
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
        import kotlinx.coroutines.delay
        import kotlinx.coroutines.launch


        class GateEntryActivity : AppCompatActivity() {

            private lateinit var binding: ActivityGateEntryBinding
            private lateinit var gateTransactionViewModel: GateTransactionViewModel
            private lateinit var progress: ProgressDialog

            private val coilList = mutableListOf<String>()
            private lateinit var transporterAdapter: TransporterAutoAdapter
            private var gateEntryNo: String = ""   // ✅ IMPORTANT
            private var transactionId: Int = 0
            private var searchJob: Job? = null
            private var dialog: AlertDialog? = null
            private var transporterDialogAdapter: TransporterDialogAdapter? = null
            private var transporterDialog: Dialog? = null
            private var fullList: List<TransporterResponse> = emptyList()
            private var transporterList: List<TransporterResponse> = emptyList()
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)

                binding = DataBindingUtil.setContentView(this, R.layout.activity_gate_entry)

                binding.idLayoutHeader.tvTitle.text = "Gate Entry"
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

                binding.etTransporterCode.threshold = 1
//                binding.etTransporterCode.setOnClickListener {
//                    gateTransactionViewModel.getTransporterList()
//                }
                binding.etTransporterCode.addTextChangedListener(object : TextWatcher {

                    override fun afterTextChanged(s: Editable?) {

                        searchJob?.cancel()

                        searchJob = lifecycleScope.launch {
                            delay(300) // debounce

                            val query = s.toString().trim()

                            if (query.isNotEmpty()) {
                                gateTransactionViewModel.getTransporterList()
                                // OR better: pass query to API
                            }
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
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

                            val list = resource.data ?: emptyList()

//                            transporterList = list
//                            fullList = list   // if you still need dialog filtering

                            // If using AutoCompleteTextView
                            transporterAdapter = TransporterAutoAdapter(this, list)
                            binding.etTransporterCode.setAdapter(transporterAdapter)
                            binding.etTransporterCode.threshold = 1

                            binding.etTransporterCode.setOnItemClickListener { parent, view, position, id ->

                                val item = parent.getItemAtPosition(position) as TransporterResponse

                                binding.etTransporterCode.setText(
                                    "${item.transporterCode}"
                                )
                                binding.etTransporterName.setText("${item.transporterName}")
                                binding.etTransporterCode.setSelection(binding.etTransporterCode.text.length)
                            }
//                            // If you want dialog also
//                            showDialog(list)
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

                        val request = GateTransactionRequest(
                            GateTransactionId = 0,
                            LocationId = null,
                            GateEntryType = "Inward",
                            VehicleNumber = binding.etVehicleNo.text.toString(),
                            TransporterName = binding.etTransporterName.text.toString(),
                            TransporterNo = "",
                            LRNumber = binding.etLrNo.text.toString(),
                            GateEntryNo = null
                        )

                        gateTransactionViewModel.createGateEntry(request)
                    }



                }





          }
