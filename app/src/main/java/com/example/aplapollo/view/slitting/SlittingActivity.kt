            package com.example.aplapollo.view.slitting
        
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
            import com.example.aplapollo.api.RetrofitInstance
            import com.example.aplapollo.helper.Constants
            import com.example.aplapollo.helper.Constants.BarcodeValue
            import com.example.aplapollo.helper.Constants.GradeV
            import com.example.aplapollo.helper.Constants.HrSlittingId
            import com.example.aplapollo.helper.Constants.JobId
            import com.example.aplapollo.helper.Constants.LocationId
            import com.example.aplapollo.helper.Constants.LocationName
            import com.example.aplapollo.helper.Constants.MotherWeightV
            import com.example.aplapollo.helper.Constants.SelectFromPlan
            import com.example.aplapollo.helper.Constants.SelectStationFirstError
            import com.example.aplapollo.helper.Constants.SourceStockId
            import com.example.aplapollo.helper.Constants.SupplierNo
            import com.example.aplapollo.helper.Constants.ThicknessV
            import com.example.aplapollo.helper.Constants.WidthId
            import com.example.aplapollo.helper.Constants.WithOutPlan
            import com.example.aplapollo.helper.Resource
            import com.example.aplapollo.helper.SessionManager
            import com.example.aplapollo.model.LocationPaginationRequest
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
                    val retrofitInstance =
                        RetrofitInstance.getInstance(applicationContext)
                    val viewModelProviderFactory = LocationViewModelFactory(application, retrofitInstance)
                    locationViewModel = ViewModelProvider(this, viewModelProviderFactory)[LocationViewModel::class.java]
                    val viewModelProviderFactorySlitting = SlittingViewModelfactory(application, retrofitInstance)
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
                    selectedLocationId?.let { slittingViewModel.getOngoingSlittingJobs(locationId = it) }
                    ongoingJobAdapter = OngoingJobAdapter(emptyList()) { selectedJob ->
                        if (selectedLocationId == null) {
                            Toasty.warning(this, "Please select a location first").show()
                            return@OngoingJobAdapter
                        }
        
                        val intent = Intent(this, SlittingStatusActivity::class.java)
                        intent.putExtra(JobId, selectedJob.jobNumber)
                        intent.putExtra(HrSlittingId, selectedJob.hrSlittingTranId)
                        intent.putExtra(SourceStockId, selectedJob.sourceStockId)
                        intent.putExtra(LocationId, selectedJob.locationId)
                        intent.putExtra(MotherWeightV, selectedJob.stockTransaction?.weight.toString())
                        intent.putExtra(BarcodeValue, selectedJob.stockTransaction?.barcode)
                        intent.putExtra(SupplierNo, selectedJob.stockTransaction?.supplierBatchNo)
                        intent.putExtra(WidthId, selectedJob.stockTransaction?.width)
                        intent.putExtra(ThicknessV, selectedJob.stockTransaction?.thickness)
                        intent.putExtra(GradeV, selectedJob.stockTransaction?.grade)
                        startActivity(intent)
                    }
        
                    binding.rvOngoingJobs.apply {
                        layoutManager = LinearLayoutManager(this@SlittingActivity)
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
                    slittingViewModel.ongoingJobsLiveData.observe(this) { resource ->
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
        
                    stationDropdown.setOnItemClickListener { _, _, position, _ ->
        
                        val selectedLocation =
                            (locationViewModel.locationListMutableLiveData.value as? Resource.Success)
                                ?.data
                                ?.get(position)
        
                        selectedLocation?.let {
                            selectedLocationId = it.locationId
                            selectedLocationName = it.locationName

//                            session.saveSelectedLocation(it.locationId, it.locationName)


                            slittingViewModel.getOngoingSlittingJobs(it.locationId)
//                            Log.d(
//                                "STATION_SELECTED",
//                                "ID=$selectedLocationId, NAME=$selectedLocationName"
//                            )
        
                        }
                    }
        
                    val coilOptions = listOf(
                        SelectFromPlan,
                        WithOutPlan,
        
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
                                    Toasty.warning(this, SelectStationFirstError).show()
                                    return@setOnItemClickListener
                                }
        
                                val intent = Intent(this, SlittingPlan2Activity::class.java)
                                intent.putExtra(LocationId, selectedLocationId!!)
                                intent.putExtra(LocationName, selectedLocationName ?: "")
                                startActivity(intent)
                            }
                            1 -> {
                                if (selectedLocationId == null) {
                                Toasty.warning(this, SelectStationFirstError).show()
                                return@setOnItemClickListener
                            }
        
                                val intent = Intent(this, Slittingplan3Activity::class.java)
                                intent.putExtra(LocationId, selectedLocationId!!)
                                startActivity(intent)
                            }
        
                        }
        
                    }
                   binding.tilAddNewCoil.setEndIconTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
                    )
        
                }
                override fun onResume() {
                    super.onResume()
                    selectedLocationId?.let { slittingViewModel.getOngoingSlittingJobs(locationId = it) }
                }
        
        
            }