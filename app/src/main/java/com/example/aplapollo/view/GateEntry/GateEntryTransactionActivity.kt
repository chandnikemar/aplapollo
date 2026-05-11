package com.example.aplapollo.view.GateEntry

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.GateEntryHistoryAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.viewmodel.gateentry.GateEntryViewModelFactory
import com.example.aplapollo.viewmodel.gateentry.GateTransactionViewModel
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGateEntryTransactionBinding
import es.dmoral.toasty.Toasty

class GateEntryTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGateEntryTransactionBinding
    private lateinit var gateTransactionViewModel: GateTransactionViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var adapter: GateEntryHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_gate_entry_transaction
        )

        binding.idLayoutHeader.tvTitle.text = "Gate Entry Transaction"
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

        // ---------------- Adapter ----------------
        adapter = GateEntryHistoryAdapter(mutableListOf()) { selectedItem ->
            val intent = Intent(this, GateEntryTransactionUpdateActivity::class.java)
            intent.putExtra("GATE_ENTRY_ID", selectedItem.gateTransactionId)
            startActivity(intent)
        }

        binding.rvGateEntryHistory.layoutManager =
            LinearLayoutManager(this)

        binding.rvGateEntryHistory.adapter = adapter

        // ---------------- Observe CORRECT LiveData ----------------
        gateTransactionViewModel.gateEntryListLiveData.observe(this) { response ->

            when (response) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val list = response.data ?: emptyList()
                    adapter.updateList(list)


                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, response.message ?: "Error").show()
                }

                else -> {}
            }
        }

        // ---------------- CALL API (ONLY ONCE) ----------------
        gateTransactionViewModel.getGateEntryList()

        // ---------------- Button ----------------
        binding.btnInProgress.setOnClickListener {
            val intent = Intent(this, GateEntryActivity::class.java)
            launcher.launch(intent)
        }
    }
    private val launcher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val isRefresh = result.data?.getBooleanExtra("IS_REFRESH", false)

                if (isRefresh == true) {
                    // ✅ CALL API AGAIN
                    gateTransactionViewModel.getGateEntryList()
                }
            }
        }
    override fun onResume() {
        super.onResume()
        gateTransactionViewModel.getGateEntryList()
    }
}