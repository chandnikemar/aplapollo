package com.example.aplapollo.view

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.CoilAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.Coils
import com.example.aplapollo.model.GateEntry.GateTransactionRequest
import com.example.aplapollo.viewmodel.gateentry.GateEntryViewModelFactory
import com.example.aplapollo.viewmodel.gateentry.GateTransactionViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGateEntryBinding
import es.dmoral.toasty.Toasty


    class GateEntryActivity : AppCompatActivity() {

        private lateinit var binding: ActivityGateEntryBinding
        private lateinit var gateTransactionViewModel: GateTransactionViewModel
        private lateinit var progress: ProgressDialog

        private lateinit var adapter: CoilAdapter
        private val coilList = mutableListOf<String>()

        private var gateEntryNo: String = ""   // ✅ IMPORTANT

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

            // ✅ RecyclerView
            adapter = CoilAdapter(coilList)
            binding.rvCoilList.layoutManager = LinearLayoutManager(this)
            binding.rvCoilList.adapter = adapter


            gateTransactionViewModel.gateEntryLiveData.observe(this) { response ->

                when (response) {

                    is Resource.Loading -> progress.show()

                    is Resource.Success -> {
                        progress.dismiss()

                        val data = response.data?.responseObject

                        if (data != null) {
                            gateEntryNo = data.gateEntryNo   // ✅ store

                            onGateEntrySuccess(gateEntryNo)

                            Toasty.success(
                                this,
                                response.data.responseMessage ?: "Success"
                            ).show()
                        }
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        Toasty.error(this, response.message ?: "Error").show()
                    }
                }
            }

            gateTransactionViewModel.gateTransactionLiveData.observe(this) { response ->

                when (response) {

                    is Resource.Loading -> progress.show()

                    is Resource.Success -> {
                        progress.dismiss()

                        Toasty.success(
                            this,
                            response.data?.responseMessage ?: "Saved Successfully"
                        ).show()

                        // ✅ Clear after submit
                        coilList.clear()
                        adapter.notifyDataSetChanged()
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        Toasty.error(this, response.message ?: "Error").show()
                    }
                }
            }


            binding.btnSubmit.setOnClickListener {

                val request = GateTransactionRequest(
                    GateTransactionId = 0,
                    LocationId = null,
                    GateEntryType = "Inward",
                    VehicleNumber = binding.etVehicleNo.text.toString(),
                    TransporterName = binding.etTransporter.text.toString(),
                    TransporterNo = binding.etTransporterNo.text.toString(),
                    LRNumber = binding.etLrNo.text.toString(),
                    GateEntryNo = null
                )

                gateTransactionViewModel.createGateEntry(request)
            }


            binding.btnAddCoil.setOnClickListener {

                val coil = binding.etCoilNo.text.toString().trim()

                if (coil.isEmpty()) {
                    binding.etCoilNo.error = "Enter Coil No"
                    return@setOnClickListener
                }

                if (coilList.contains(coil)) {
                    binding.etCoilNo.error = "Duplicate Coil"
                    return@setOnClickListener
                }

                coilList.add(coil)
                adapter.notifyItemInserted(coilList.size - 1)

                binding.etCoilNo.text?.clear()
                binding.rvCoilList.visibility = View.VISIBLE
                binding.btnFinalSubmit.visibility = View.VISIBLE
            }



            binding.btnFinalSubmit.setOnClickListener {

                if (coilList.isEmpty()) {
                    Toast.makeText(this, "Add at least one coil", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val coilItems = coilList.map {
                    Coils(coilBatch = it)
                }

                if (coilList.isEmpty()) {
                    binding.btnFinalSubmit.visibility = View.GONE
                } else {
                    binding.btnFinalSubmit.visibility = View.VISIBLE
                }
                if (coilList.isEmpty()) {
                    binding.btnFinalSubmit.visibility = View.GONE
                } else {
                    binding.btnFinalSubmit.visibility = View.VISIBLE
                }
                val request = CoilSubmitRequest(
                    gateTransactionItemId = 0,
                    gateTransactionId = 0,
                    gateEntryNo = gateEntryNo,
                    gateTransactionItems = coilItems
                )

                gateTransactionViewModel.saveCoilItem(request)
            }
        }


        private fun onGateEntrySuccess(gateEntryNo: String) {

            binding.tvGateEntryNo.text = "Gate Entry No: $gateEntryNo"

            binding.tvGateEntryNo.visibility = View.VISIBLE
            binding.layoutCoilSection.visibility = View.VISIBLE

            binding.btnSubmit.isEnabled = false
        }
    }
