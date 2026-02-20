package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
import com.example.aplapollo.model.Pickling.ProcessPicklingRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingOutwardBinding
import es.dmoral.toasty.Toasty

class PicklingOutwardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPicklingOutwardBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var picklingViewModel: PicklingViewModel

    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private var tenantCode: String? = null
    private var userDetail: HashMap<String, Any?>? = null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null

    private var locationId: Int = 0
    private var sourceStockId: Int = 0
    private var transactionId: Int = 0
    private var tranPlanId: Int = 0
    private var barcode: String = ""
    private var motherWeight: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling_outward)
        binding.idLayoutHeader.tvTitle.text = "Hr Pickling Inward"
        supportActionBar?.hide()

        progress = ProgressDialog(this).apply { setMessage("Please Wait...") }

        // Initialize session and ViewModel
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
        val factory = PicklingViewModelfactory(application, retrofitInstance)
        picklingViewModel = ViewModelProvider(this, factory)[PicklingViewModel::class.java]

        // Back button
        binding.idLayoutHeader.ivBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Check user details
        if (userDetail.isNullOrEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
            return
        } else {
            token = userDetail!!["jwtToken"] as? String
            userName = userDetail!!["userName"] as? String
            tenantCode = userDetail!!["defaultTenantCode"] as? String
            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP] as? String
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP] as? String
            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"

            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tenant_Code", "Tenant Code = $tenantCode")
        }

        // Get Intent extras safely
        transactionId = intent.getIntExtra("PICKLING_ID", 0)
        locationId = intent.getIntExtra("LOCATION_ID", 0)

        if (transactionId == 0) {
            Toasty.error(this, "Invalid Transaction ID").show()
            finish()
            return
        }

        // Disable Iron Loss field
        binding.layoutScrapTable.tvIronLossValue.isEnabled = false
        binding.btnSave.isEnabled = false

        picklingViewModel.fetchPicklingTransactionById(transactionId)

        picklingViewModel.picklingTransactionLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> showProgress()
                is Resource.Success -> {
                    dismissProgress()
                    val transaction = resource.data
                    if (transaction == null) {
                        Toasty.error(this, "No transaction data found").show()
                        return@observe
                    }

                    bindTransactionData(transaction)
                    binding.btnSave.isEnabled = true

                }
                is Resource.Error -> {
                    dismissProgress()
                    Toasty.error(this, resource.message ?: "Error fetching transaction").show()
                }
            }
        }


        picklingViewModel.processPicklingLiveData.observe(this) { result ->
            when (result) {
                is Resource.Loading -> showProgress()
                is Resource.Success -> {
                    dismissProgress()
                    Toast.makeText(this, result.data, Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    dismissProgress()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }


        binding.layoutScrapTable.etScrapWeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { calculateAndShowIronLoss() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Save button
        binding.btnSave.setOnClickListener { submitPickling() }
    }


    private fun bindTransactionData(transaction: PicklingTransactionResponse) {
        binding.textJobNumber.text = "Job #${transaction.jobNumber}"
        binding.tvMotherCoil.setText(transaction.motherBarcode)
        binding.tvBatchNumber.setText(transaction.motherCoilWeight.toString())
        binding.jobTable.textC2?.setText(transaction.barcode)

        barcode = transaction.barcode ?: ""
        motherWeight = transaction.motherCoilWeight ?: 0.0
        sourceStockId = transaction.sourceStockId
        tranPlanId = transaction.picklingTranId
        tenantCode=transaction.tenantCode

        calculateAndShowIronLoss()
    }


    private fun calculateAndShowIronLoss() {
        val scrapWeight = binding.layoutScrapTable.etScrapWeight?.text.toString().toDoubleOrNull() ?: 0.0
        val childWeight = binding.jobTable.editC4?.text.toString().toDoubleOrNull() ?: 0.0

        val calculatedIronLoss = motherWeight - (childWeight + scrapWeight)
        val ironLoss = if (calculatedIronLoss < 0) 0.0 else calculatedIronLoss

        binding.layoutScrapTable.tvIronLossValue?.setText(String.format("%.2f", ironLoss))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitPickling() {
        val scrapWeight = binding.layoutScrapTable.etScrapWeight?.text.toString().toDoubleOrNull() ?: 0.0


        val enteredWeight = binding.jobTable.editC4?.text.toString().toDoubleOrNull() ?: 0.0

        val calculatedIronLoss = motherWeight - (enteredWeight + scrapWeight)
        val ironLoss = if (calculatedIronLoss < 0) 0.0 else calculatedIronLoss

        val request = ProcessPicklingRequest(
            picklingTranId = tranPlanId,
            tenantCode = tenantCode ?: "",
            locationId = locationId,
            sourceStockId = sourceStockId,
            jobNumber ="",
            barcode = barcode,
            ironLossWeight = ironLoss,
            scrapWeight = scrapWeight,
            weightAfterPickling = enteredWeight,
            completedBy = userName ?: "",
            completedDate = getCurrentDateTimeISO(),
            status = "Completed",
            remarks = "Pickling Completed",
            isDivided = false
        )

        Log.d("PICKLING_POST", "Request Body = $request")
        picklingViewModel.submitPickling(request)
    }



    private fun showProgress() {
        if (!progress.isShowing) progress.show()
    }

    /** Dismiss ProgressDialog safely */
    private fun dismissProgress() {
        if (::progress.isInitialized && progress.isShowing) progress.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissProgress()
    }
}
