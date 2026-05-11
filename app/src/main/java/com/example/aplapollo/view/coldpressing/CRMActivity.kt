package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Coldpressing.OngoingCRMJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.CrmTranJob
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Constants.LocationName
import com.example.aplapollo.helper.Constants.SelectFromPlan
import com.example.aplapollo.helper.Constants.WithOutPlan
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmactivityBinding
import es.dmoral.toasty.Toasty

class CRMActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrmactivityBinding
    private lateinit var progress: ProgressDialog
    private lateinit var crmViewModel: CRMViewModel
    private lateinit var session: SessionManager

    private var userName: String? = ""
    private var tenantCode: String? = ""

    // ✅ FIXED LOCATION (CHANGE IF NEEDED)
    private var locationId: Int = 1
    private var locationName: String = "Default Location"

    private lateinit var ongoingJobAdapter: OngoingCRMJobAdapter
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmactivity)
        binding.idLayoutHeader.tvTitle.text = "Cold Pressing"
        supportActionBar?.hide()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
        val factory = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, factory)[CRMViewModel::class.java]

        session = SessionManager(this)
        val userDetail = session.getUserDetails()

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (userDetail!!.isNotEmpty()) {
            userName = userDetail["userName"].toString()
            tenantCode = userDetail["defaultTenantCode"].toString()

            Log.d("USER", "User = $userName")
        }
        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)

        locationId = intent.getIntExtra(Constants.LocationId, -1)
        selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""

        Log.d("LOCATION_ID", "Received locationId = $locationId")

        if (locationId != -1) {
            crmViewModel.getOngoingCRMJobs(locationId)

        } else {
            Toasty.error(this, "Invalid LocationId").show()
        }
        ongoingJobAdapter = OngoingCRMJobAdapter(emptyList()) { selectedJob ->

            val intent = Intent(this, CRMTransactionActivity::class.java)
            intent.putExtra(LocationId, locationId)
            intent.putExtra(LocationName, locationName)
            intent.putExtra(CrmTranJob, selectedJob.crmTranId)
            intent.putExtra("GRADE", selectedJob.grade)
            intent.putExtra("PROCESS_NAME", selectedProcessName)
            intent.putExtra("MACHINE_NAME", selectedMachineName)

            startActivity(intent)
        }

        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        binding.rvOngoingJobs.adapter = ongoingJobAdapter

        // ---------------- Load Jobs ----------------
        crmViewModel.getOngoingCRMJobs(locationId)

        crmViewModel.ongoingJobsLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val jobs = resource.data ?: emptyList()

                    if (jobs.isEmpty()) {
                        Toasty.info(this, "No ongoing jobs found").show()
                        ongoingJobAdapter.updateList(emptyList())
                    } else {
                        ongoingJobAdapter.updateList(jobs)
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, resource.message ?: "Error").show()
                }

                else -> {}
            }
        }

        // ---------------- Dropdown (Plan / Without Plan) ----------------
        val coilOptions = listOf(SelectFromPlan, WithOutPlan)

        val coilAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            coilOptions
        )

        binding.ddAddNewCoil.setAdapter(coilAdapter)

        binding.ddAddNewCoil.setOnItemClickListener { _, _, position, _ ->

            when (position) {

                0 -> {
                    val intent = Intent(this, CRMPlanActivity::class.java)
                    intent.putExtra(LocationId, locationId)
                    intent.putExtra(LocationName, locationName)
                    startActivity(intent)
                }

                1 -> {
                    val intent = Intent(this, CRMPlanOutwardActivity::class.java)
                    intent.putExtra(LocationId, locationId)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        crmViewModel.getOngoingCRMJobs(locationId)
    }
}