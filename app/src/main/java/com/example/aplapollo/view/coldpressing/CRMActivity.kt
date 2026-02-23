package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Coldpressing.OngoingCRMJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.aplapollo.viewmodel.location.LocationViewModel
import com.example.aplapollo.viewmodel.location.LocationViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmactivityBinding
import es.dmoral.toasty.Toasty

class CRMActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmactivityBinding
    private lateinit var progress: ProgressDialog

    private lateinit var locationViewModel: LocationViewModel
    private  lateinit var crmViewModel: CRMViewModel
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var selectedLocationId: Int? = null
    private var selectedLocationName: String? = null
    private lateinit var ongoingJobAdapter: OngoingCRMJobAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmactivity)
        binding.idLayoutHeader.tvTitle.text = "Cold Pressing"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        val viewModelProviderFactory = LocationViewModelFactory(application, retrofitInstance)
        locationViewModel = ViewModelProvider(this, viewModelProviderFactory)[LocationViewModel::class.java]

        val viewModelProviderFactorys = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, viewModelProviderFactorys)[CRMViewModel::class.java]
        session = SessionManager(this)
        userDetail = session.getUserDetails()
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
            binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)

            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val stationDropdown = binding.includeShiftStation.dropdownFields
        binding.includeShiftStation.textOperator.setText(userName)
        val locationRequest = LocationPaginationRequest(
            locationId = 0,
            locationName = "",
            locationCode = "",
            locationType = null,
            displayName = null,
            parentLocationId = null,
            isActive = true,
            rowSize = 10,
            currentPage = 1
        )
        val coilOptions = listOf(
            "Select from plan",
            "Without plan",
            )


        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        locationViewModel.getLocations( locationRequest)
        selectedLocationId?.let { crmViewModel.getOngoingCRMJobs(it) }
        ongoingJobAdapter = OngoingCRMJobAdapter(emptyList()) { selectedJob ->
            if (selectedLocationId == null) {
                Toasty.warning(this, "Please select a location first").show()
                return@OngoingCRMJobAdapter
            }

            val intent = Intent(this, CRMTransactionActivity::class.java)

            intent.putExtra("LOCATION_ID", selectedLocationId!!)
            intent.putExtra("LOCATION_NAME", selectedLocationName ?: "")
            intent.putExtra("CRM_TRAN_JOB", selectedJob.crmTranId )
            startActivity(intent)
        }

        binding.rvOngoingJobs.apply {
            layoutManager = LinearLayoutManager(this@CRMActivity)
            adapter = ongoingJobAdapter
        }
        locationViewModel.locationListMutableLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    progress.show()

                }

                is Resource.Success -> {
                    progress.dismiss()
                    val locations = resource.data ?: emptyList()


                    val stationNames = locations.map { it.locationName }

                    val adapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        stationNames
                    )
                    binding.includeShiftStation.dropdownFields.setAdapter(adapter)
                }

                is Resource.Error -> {
                    Toast.makeText(
                        this,
                        resource.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {}
            }
        }
        crmViewModel.ongoingJobsLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> progress.show()
                is Resource.Success -> {
                    progress.dismiss()
                    val jobs = resource.data ?: emptyList()
                    selectedLocationName?.let {
                        binding.includeShiftStation.dropdownFields.setText(it, false)
                    }
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

        stationDropdown.setOnItemClickListener { _, _, position, _ ->

            val selectedLocation =
                (locationViewModel.locationListMutableLiveData.value as? Resource.Success)
                    ?.data
                    ?.get(position)

            selectedLocation?.let {
                selectedLocationId = it.locationId
                selectedLocationName = it.locationName
                crmViewModel.getOngoingCRMJobs(it.locationId)
                Log.d(
                    "STATION_SELECTED",
                    "ID=$selectedLocationId, NAME=$selectedLocationName"
                )

            }
        }
        val coilAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            coilOptions
        )
        binding.ddAddNewCoil.setAdapter(coilAdapter)
        binding.ddAddNewCoil.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    if (selectedLocationId == null) {
                        Toasty.warning(this, "Please select station first").show()
                        return@setOnItemClickListener
                    }

                    val intent = Intent(this, CRMPlanActivity::class.java)
                    intent.putExtra("LOCATION_ID", selectedLocationId!!)
                    intent.putExtra("LOCATION_NAME", selectedLocationName ?: "")
                    startActivity(intent)
                }
                1 -> {
                    if (selectedLocationId == null) {
                        Toasty.warning(this, "Please select station first").show()
                        return@setOnItemClickListener
                    }

                    val intent = Intent(this, CRMPlanOutwardActivity::class.java)
                    intent.putExtra("Location_ID", selectedLocationId!!)
                    startActivity(intent)
                }

            }

        }
        binding.tilAddNewCoil.setEndIconTintList(
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        )

//            startActivity(Intent(this@CRMActivity, CRMPlanActivity::class.java))
        }
    override fun onResume() {
        super.onResume()
        selectedLocationId?.let { crmViewModel.getOngoingCRMJobs(it) }
    }



    }
