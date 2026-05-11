package com.example.aplapollo.view.GateEntry

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.CoilAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.Coils
import com.example.aplapollo.viewmodel.gateentry.GateEntryViewModelFactory
import com.example.aplapollo.viewmodel.gateentry.GateTransactionViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGateEntryTransactionUpdateBinding
import es.dmoral.toasty.Toasty

class GateEntryTransactionUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGateEntryTransactionUpdateBinding
    private lateinit var gateTransactionViewModel: GateTransactionViewModel
    private lateinit var progress: ProgressDialog
    private var gateTransactionId: Int = 0
    private lateinit var adapter: CoilAdapter
    private val coilList = mutableListOf<String>()

    private var gateEntryNo: String = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_gate_entry_transaction_update
        )
        binding.idLayoutHeader.tvTitle.text = "Gate Entry Update"
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        supportActionBar?.hide()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        // ---------------- ViewModel ----------------
        val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
        val factory = GateEntryViewModelFactory(application, retrofitInstance)

        gateTransactionViewModel =
            ViewModelProvider(this, factory)[GateTransactionViewModel::class.java]
        gateTransactionId = intent.getIntExtra("GATE_ENTRY_ID", 0)
        if (gateTransactionId != 0) {
            gateTransactionViewModel.getGateEntryUpdate(gateTransactionId)
        }

        adapter = CoilAdapter(coilList)
        binding.rvCoilList.layoutManager =
            LinearLayoutManager(this)
        binding.rvCoilList.adapter = adapter
        binding.rvCoilList.isNestedScrollingEnabled = false

        binding.btnFinalSubmit.visibility = View.GONE
        gateTransactionViewModel.gateEntryUpdateLiveData.observe(this) { response ->

            when (response) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val data = response.data?.firstOrNull()
                    data?.let {

                        // ✅ Store values
                        gateEntryNo = it.gateEntryNo

                        // ✅ Bind UI
                        binding.tvGateEntryNo.text = "Gate Entry No: ${it.gateEntryNo}"
                        binding.tvTransporter.text = "Transporter: ${it.transporterName}"
                        binding.tvTransporterNo.text = "Transporter No: ${it.transporterNo}"
                        binding.tvVehicle.text = "Vehicle: ${it.vehicleNumber}"
                        binding.tvLrNo.text = "LR No: ${it.lrNumber}"

                        // ✅ Show coil section
                        binding.layoutCoilSection.visibility = View.VISIBLE

                    }}

                is Resource.Error -> {

                    progress.dismiss()

                    val errorMsg = response.message ?: "Unknown Error"

                    Log.e("GateTransactionError", errorMsg)
                   Utils.showErrorDialog(this, errorMsg)
                }

                else -> {}
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

                    coilList.clear()
                    adapter.notifyDataSetChanged()
                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()

                    val errorMsg = response.message ?: "Unknown Error"

                    Log.e("API_ERROR", errorMsg)

                    Utils.showErrorDialog(this, errorMsg)

                }

                else -> {}
            }
        }
        binding.btnAddCoil.setOnClickListener {

            val coil = binding.etCoilNo.text.toString().trim()

            when {
                coil.isEmpty() -> {
                    binding.etCoilNo.error = "Enter Coil No"
                }

                coilList.contains(coil) -> {
                    binding.etCoilNo.error = "Duplicate Coil"
                }

                else -> {

                    coilList.add(coil)

                    adapter.notifyDataSetChanged()

                    binding.etCoilNo.text?.clear()

                    binding.rvCoilList.visibility = View.VISIBLE

                    binding.btnFinalSubmit.visibility = View.VISIBLE
                }
            }
        }



        binding.btnFinalSubmit.setOnClickListener {

            if (coilList.isEmpty()) {
                Utils.showErrorDialog(this,"Add at least one coil")
//                Toast.makeText(this, "Add at least one coil", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (gateEntryNo.isEmpty()) {
                Utils.showErrorDialog(this,"Gate Entry not loaded")
//                Toast.makeText(this, "Gate Entry not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val coilItems = coilList.map {
                Coils(coilBatch = it)
            }
            binding.btnFinalSubmit.visibility =
                if (coilList.isEmpty()) View.GONE else View.VISIBLE
            val request = CoilSubmitRequest(
                gateTransactionItemId = 0,
                gateTransactionId =gateTransactionId ,
                gateEntryNo = gateEntryNo,
                gateTransactionItems = coilItems
            )

            gateTransactionViewModel.saveCoilItem(request)
        }

    }


}