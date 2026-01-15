    package com.example.aplapollo.view.Slitting

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
    import com.example.aplapollo.adapter.Slitting.OngoingJobAdapter
    import com.example.aplapollo.helper.Constants
    import com.example.aplapollo.helper.LogoutHelper
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionExpiredEvent
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.model.LocationPaginationRequest
    import com.example.aplapollo.repository.APLRepository
    import com.example.aplapollo.viewmodel.location.LocationViewModel
    import com.example.aplapollo.viewmodel.location.LocationViewModelFactory
    import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
    import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
    import com.example.apolloapl.R
    import com.example.apolloapl.databinding.ActivitySlittingBinding
    import es.dmoral.toasty.Toasty

    class SlittingActivity : AppCompatActivity() {
            private lateinit var binding: ActivitySlittingBinding
        private lateinit var progress: ProgressDialog
        private lateinit var locationViewModel: LocationViewModel
        private  lateinit var  slittingViewModel:SlittingViewModel

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
        private lateinit var ongoingJobAdapter: OngoingJobAdapter



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting)

            binding.idLayoutHeader.tvTitle.text = "On Going Job"
            supportActionBar?.hide()
            progress = ProgressDialog(this)
            progress.setMessage("Please Wait...")
            val aplRepository = APLRepository()
            val viewModelProviderFactory = LocationViewModelFactory(application, aplRepository)
            locationViewModel = ViewModelProvider(this, viewModelProviderFactory)[LocationViewModel::class.java]
            val viewModelProviderFactorySlitting = SlittingViewModelfactory(application, aplRepository)
            slittingViewModel = ViewModelProvider(this, viewModelProviderFactorySlitting)[SlittingViewModel::class.java]
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
            SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
                if (shouldLogout == true) {
                    SessionExpiredEvent.logoutLiveData.value = false
                    LogoutHelper.handleLogout(this, session)
                }
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
            locationViewModel.getLocations(baseUrl, locationRequest)
            slittingViewModel.getOngoingSlittingJobs(baseUrl)
            ongoingJobAdapter = OngoingJobAdapter(emptyList()) { selectedJob ->
                val intent = Intent(this, SlittingStatusActivity::class.java)
                intent.putExtra("JOB_ID", selectedJob.jobNumber)
                intent.putExtra("HrSlitting_planID", selectedJob.hrSlittingTranId)
                intent.putExtra("Source_StockID", selectedJob.sourceStockId)
                intent.putExtra("Location_ID", selectedJob.locationId)
                intent.putExtra("Mother_Weight", selectedJob.stockTransaction?.weight.toString())
                intent.putExtra("BARCODE", selectedJob.stockTransaction?.barcode)
                intent.putExtra("SupplierNo", selectedJob.stockTransaction?.supplierBatchNo)
                intent.putExtra("WIDTH", selectedJob.stockTransaction?.width)
                intent.putExtra("THICKNESS", selectedJob.stockTransaction?.thickness)
                intent.putExtra("GRADE", selectedJob.stockTransaction?.grade)
                startActivity(intent)
            }

            binding.rvOngoingJobs.apply {
                layoutManager = LinearLayoutManager(this@SlittingActivity)
                adapter = ongoingJobAdapter
            }

            locationViewModel.locationListMutableLiveData.observe(this) { resource ->
                when (resource) {

                    is Resource.Loading -> {

                    }

                    is Resource.Success -> {
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
            slittingViewModel.ongoingJobsLiveData.observe(this) { resource ->
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

            stationDropdown.setOnItemClickListener { _, _, position, _ ->

                val selectedLocation =
                    (locationViewModel.locationListMutableLiveData.value as? Resource.Success)
                        ?.data
                        ?.get(position)

                selectedLocation?.let {
                    selectedLocationId = it.locationId
                    selectedLocationName = it.locationName

                    Log.d(
                        "STATION_SELECTED",
                        "ID=$selectedLocationId, NAME=$selectedLocationName"
                    )

                }
            }

            val coilOptions = listOf(
                "Select from plan",
                "Without plan",

            )

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

                        val intent = Intent(this, SlittingPlan2Activity::class.java)
                        intent.putExtra("LOCATION_ID", selectedLocationId!!)
                        intent.putExtra("LOCATION_NAME", selectedLocationName ?: "")
                        startActivity(intent)
                    }
                    1 -> {
                        if (selectedLocationId == null) {
                        Toasty.warning(this, "Please select station first").show()
                        return@setOnItemClickListener
                    }

                        val intent = Intent(this, Slittingplan3Activity::class.java)
                        intent.putExtra("Location_ID", selectedLocationId!!)
                        startActivity(intent)
                    }

                }

            }
           binding.tilAddNewCoil.setEndIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
            )

        }


    }