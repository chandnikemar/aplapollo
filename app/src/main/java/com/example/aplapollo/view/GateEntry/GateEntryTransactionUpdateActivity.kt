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
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.CoilAdapter
import com.example.aplapollo.adapter.GateEntry.AddedCoilAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.GateEntry.CoilSubmitRequest
import com.example.aplapollo.model.GateEntry.Coils
import com.example.aplapollo.model.GateEntry.GateTransactionItem
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
    private val savedCoilList = mutableListOf<String>()
    private val gateTransactionItems =
        mutableListOf<GateTransactionItem>()

    private var gateEntryNo: String = ""
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_gate_entry_transaction_update
        )
        binding.idLayoutHeader.tvTitle.text = "Gate Entry Details"
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

        adapter = CoilAdapter(
            coilList,
            savedCoilList
        )
        binding.rvCoilList.layoutManager =
            LinearLayoutManager(this)
        binding.rvCoilList.adapter = adapter
        binding.rvCoilList.isNestedScrollingEnabled = false

        binding.btnFinalSubmit.visibility = View.GONE
        binding.btnView.setOnClickListener {

            val activeItems = gateTransactionItems.filter {
                it.isActive == true
            }

            Log.d(
                "AddedCoilsDialog",
                "Active Coil Count: ${activeItems.size}"
            )

            activeItems.forEach {

                Log.d(
                    "AddedCoilsDialog",
                    "CoilBatch: ${it.coilBatch}"
                )
            }

            if (activeItems.isEmpty()) {

                Utils.showErrorDialog(
                    this,
                    "No active coils found"
                )

                return@setOnClickListener
            }

            val dialogView = layoutInflater.inflate(
                R.layout.dialog_added_coils,
                null
            )

            val recyclerView =
                dialogView.findViewById<RecyclerView>(
                    R.id.rvAddedCoils
                )

            recyclerView.layoutManager =
                LinearLayoutManager(this)

            recyclerView.adapter =
                AddedCoilAdapter(activeItems)

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Already Added Coils")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show()
        }
        gateTransactionViewModel.gateEntryUpdateLiveData.observe(this) { response ->

            when (response) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {

                    progress.dismiss()

                    val data = response.data?.firstOrNull()

                    data?.let {

                        gateEntryNo = it.gateEntryNo

                        // Bind UI
                        binding.tvGateEntryNo.text = "${it.gateEntryNo}"
                        binding.tvTransporter.text = "${it.transporterName}"
                        binding.tvTransporterNo.text = "${it.transporterNo}"
                        binding.tvVehicle.text = "${it.vehicleNumber}"
                        binding.tvLrNo.text = "${it.lrNumber}"

                        binding.layoutCoilSection.visibility = View.VISIBLE

                        coilList.clear()
                        savedCoilList.clear()
                        gateTransactionItems.clear()

                        it.gateTransactionItem?.forEach { item ->

                            Log.d(
                                "API_COIL",
                                "Coil: ${item.coilBatch} | Active: ${item.isActive}"
                            )


                            gateTransactionItems.add(item)

                            if (item.isActive == true) {

                                item.coilBatch?.let { coil ->

                                    savedCoilList.add(coil)
                                }
                            }
                        }

                        Log.d(
                            "API_COIL",
                            "Total Active Coils: ${savedCoilList.size}"
                        )
                        adapter.notifyDataSetChanged()


                        binding.rvCoilList.visibility =
                            if (coilList.isEmpty()) View.GONE else View.VISIBLE

                        // Show Submit Button
                        binding.btnFinalSubmit.visibility =
                            if (coilList.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

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

            binding.tilCoilNo.error = null

            when {

                coil.isEmpty() -> {

                    binding.tilCoilNo.error =
                        "Enter Coil Number"
                }

                savedCoilList.any {
                    it.equals(coil, ignoreCase = true)
                } -> {

                    binding.tilCoilNo.error =
                        "Coil already added in Gate Entry"
                }

                // Already scanned in current list
                coilList.any {
                    it.equals(coil, ignoreCase = true)
                } -> {

                    binding.tilCoilNo.error =
                        "Coil already scanned"
                }

                else -> {

                    coilList.add(coil)

                    adapter.notifyItemInserted(
                        coilList.size - 1
                    )

                    binding.etCoilNo.text?.clear()

                    binding.rvCoilList.visibility = View.VISIBLE

                    binding.btnFinalSubmit.visibility = View.VISIBLE
                }
            }
        }



        binding.btnFinalSubmit.setOnClickListener {

            if (coilList.isEmpty()) {

                Utils.showErrorDialog(
                    this,
                    "Please add at least one coil"
                )

                return@setOnClickListener
            }

            // Prevent duplicate submit
            val duplicateCoils = coilList.filter { coil ->
                savedCoilList.any {
                    it.equals(coil, ignoreCase = true)
                }
            }

            if (duplicateCoils.isNotEmpty()) {

                Utils.showErrorDialog(
                    this,
                    "These coils are already added:\n${
                        duplicateCoils.joinToString("\n")
                    }"
                )

                return@setOnClickListener
            }

            val coilItems = coilList.map {
                Coils(coilBatch = it)
            }

            val request = CoilSubmitRequest(
                gateTransactionItemId = 0,
                gateTransactionId = gateTransactionId,
                gateEntryNo = gateEntryNo,
                gateTransactionItems = coilItems
            )

            gateTransactionViewModel.saveCoilItem(request)
        }
    }


}