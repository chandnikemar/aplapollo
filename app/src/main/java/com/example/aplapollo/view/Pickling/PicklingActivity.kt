package com.example.aplapollo.view.Pickling

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Pickling.OngoingJobPicklingAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Constants.PicklingId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingBinding
import es.dmoral.toasty.Toasty

class PicklingActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPicklingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager

        private  lateinit var  picklingViewModel: PicklingViewModel
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var locationId: Int = 0

    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null

    private lateinit var ongoingJobAdapter: OngoingJobPicklingAdapter
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling)

                supportActionBar?.hide()

                val retrofitInstance =
                    RetrofitInstance.getInstance(applicationContext)
                session = SessionManager(this)
                userDetail = session.getUserDetails()

                val viewModelProviderFactoryPickling = PicklingViewModelfactory(application, retrofitInstance)
                picklingViewModel = ViewModelProvider(this, viewModelProviderFactoryPickling)[PicklingViewModel::class.java]
                binding.idLayoutHeader.ivBack.setOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
                if (userDetail!!.isEmpty()) {
                    Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
                } else {

                    token = userDetail!!["jwtToken"].toString()
                    userName = userDetail!!["userName"].toString()
                    tenantCode= userDetail!!["defaultTenantCode"].toString()

                    serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
                    serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

                    baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"

                    Log.d("JWT_TOKEN_QC", "JWT Token = $token")
                    Log.d("Tanent_Code","Tenant Code= $tenantCode")
                }
                supportActionBar?.hide()
                progress = ProgressDialog(this)
                progress.setMessage("Please Wait...")
        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        locationId = intent.getIntExtra(Constants.LocationId, -1)
        selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
        selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
        Log.d("LOCATION_ID", "Received locationId = $locationId")
        binding.idLayoutHeader.tvTitle.text = selectedProcessName+"ON GOING GOBS"

        if (locationId != -1) {
            picklingViewModel.getOngoingPicklingJobs(locationId)

        } else {
            Toasty.error(this, "Invalid LocationId").show()
        }
        ongoingJobAdapter = OngoingJobPicklingAdapter(emptyList()) { selectedJob ->



            val intent = Intent(this, PicklingOutwardActivity::class.java)

            intent.putExtra(PicklingId, selectedJob.picklingTranId)
            intent.putExtra(BarcodeValue, selectedJob.barcode)
            intent.putExtra("THICKNESS", selectedJob.thickness)
            intent.putExtra("GRADE", selectedJob.grade)
            intent.putExtra("PROCESS_NAME", selectedProcessName)
            intent.putExtra("MACHINE_NAME", selectedMachineName)
//            intent.putExtra(Constants.SourceStockId, selectedJob.)
//            intent.putExtra(Constants.LocationId, selectedJob.locationId)
            startActivity(intent)
        }

        binding.rvOngoingJobs.apply {
            layoutManager = LinearLayoutManager(this@PicklingActivity)
            adapter = ongoingJobAdapter
        }

                     binding.btnInProgress.setOnClickListener {
                    val intent = Intent(this, PicklingInwardActivity::class.java)
                         intent.putExtra(Constants.LocationId, locationId)
                         startActivity(intent)
                }

        picklingViewModel.ongoingJobsLiveData.observe(this) { resource ->
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
                        Log.d("ONGOING_JOBS", "Jobs size = ${jobs.size}")

                    }
                }
                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, resource.message ?: "Error loading jobs", Toasty.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

            }

    override fun onResume() {
        super.onResume()
        if (locationId != -1) {
            picklingViewModel.getOngoingPicklingJobs(locationId)
        }
    }
}