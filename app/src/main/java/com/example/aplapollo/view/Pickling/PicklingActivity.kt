package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Pickling.OngoingJobPicklingAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Constants.LocationName
import com.example.aplapollo.helper.Constants.PicklingId
import com.example.aplapollo.helper.Constants.WidthId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.LocationPaginationRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.location.LocationViewModel
import com.example.aplapollo.viewmodel.location.LocationViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingBinding
import es.dmoral.toasty.Toasty

class PicklingActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPicklingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var locationViewModel: LocationViewModel
        private  lateinit var  picklingViewModel: PicklingViewModel
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private var selectedLocationId: Int? = null
    private var selectedLocationName: String? = null
    private lateinit var ongoingJobAdapter: OngoingJobPicklingAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling)
                binding.idLayoutHeader.tvTitle.text = "Pickling"
                supportActionBar?.hide()
                progress = ProgressDialog(this)
                progress.setMessage("Please Wait...")
                val retrofitInstance =
                    RetrofitInstance.getInstance(applicationContext)
                session = SessionManager(this)
                userDetail = session.getUserDetails()
                val viewModelProviderFactory = LocationViewModelFactory(application, retrofitInstance)
                locationViewModel = ViewModelProvider(this, viewModelProviderFactory)[LocationViewModel::class.java]
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
        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        locationViewModel.getLocations( locationRequest)
        selectedLocationId?.let { picklingViewModel.getOngoingPicklingJobs(it) }
        ongoingJobAdapter = OngoingJobPicklingAdapter(emptyList()) { selectedJob ->

            if (selectedLocationId == null) {
                Toasty.warning(this, "Please select a location first").show()
                return@OngoingJobPicklingAdapter
            }

            val intent = Intent(this, PicklingOutwardActivity::class.java)
            intent.putExtra(LocationId, selectedLocationId!!)
            intent.putExtra(LocationName, selectedLocationName ?: "")
            intent.putExtra(PicklingId, selectedJob.picklingTranId)
            intent.putExtra(BarcodeValue, selectedJob.barcode)
            intent.putExtra(WidthId, selectedJob.width)
            intent.putExtra("THICKNESS", selectedJob.thickness)
            intent.putExtra("GRADE", selectedJob.grade)

            startActivity(intent)
        }

        binding.rvOngoingJobs.apply {
            layoutManager = LinearLayoutManager(this@PicklingActivity)
            adapter = ongoingJobAdapter
        }

        stationDropdown.setOnItemClickListener { _, _, position, _ ->

                    val selectedLocation =
                        (locationViewModel.locationListMutableLiveData.value as? Resource.Success)
                            ?.data
                            ?.get(position)

                    selectedLocation?.let {
                        selectedLocationId = it.locationId
                        selectedLocationName = it.locationName
                        selectedLocationId = selectedLocation.locationId
                        picklingViewModel.getOngoingPicklingJobs(it.locationId)
                        Log.d(
                            "STATION_SELECTED",
                            "ID=$selectedLocationId, NAME=$selectedLocationName"
                        )

                    }
                }

                binding.btnInProgress.setOnClickListener {

                    val selectedLocation =
                        binding.includeShiftStation.dropdownFields.text.toString().trim()

                    if (selectedLocationId == null) {
                        Toasty.warning(this, "Please select location first").show()
                        return@setOnClickListener
                    }

                    val intent = Intent(this, PicklingInwardActivity::class.java)
                    intent.putExtra(LocationId, selectedLocationId!!)
                    intent.putExtra(LocationName, selectedLocationName ?: "")
                    startActivity(intent)
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
        picklingViewModel.ongoingJobsLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> progress.show()
                is Resource.Success -> {
                    progress.dismiss()
                    selectedLocationName?.let {
                        binding.includeShiftStation.dropdownFields.setText(it, false)
                    }
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
        selectedLocationId?.let { picklingViewModel.getOngoingPicklingJobs(locationId = it) }
    }

}