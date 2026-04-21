            package com.example.aplapollo.view.slitting

            import android.app.ProgressDialog
            import android.content.Intent
            import android.os.Bundle
            import android.util.Log
            import android.widget.ArrayAdapter
            import androidx.appcompat.app.AppCompatActivity
            import androidx.databinding.DataBindingUtil
            import androidx.lifecycle.ViewModelProvider
            import androidx.recyclerview.widget.LinearLayoutManager
            import com.example.aplapollo.adapter.Slitting.OngoingJobAdapter
            import com.example.aplapollo.api.RetrofitInstance
            import com.example.aplapollo.helper.Constants.BarcodeValue
            import com.example.aplapollo.helper.Constants.GradeV
            import com.example.aplapollo.helper.Constants.HrSlittingId
            import com.example.aplapollo.helper.Constants.HrSlittingPlanId
            import com.example.aplapollo.helper.Constants.JobId
            import com.example.aplapollo.helper.Constants.LocationId
            import com.example.aplapollo.helper.Constants.MotherWeightV
            import com.example.aplapollo.helper.Constants.SelectFromPlan
            import com.example.aplapollo.helper.Constants.SourceStockId
            import com.example.aplapollo.helper.Constants.SupplierNo
            import com.example.aplapollo.helper.Constants.ThicknessV
            import com.example.aplapollo.helper.Constants.WidthId
            import com.example.aplapollo.helper.Constants.WithOutPlan
            import com.example.aplapollo.helper.Resource
            import com.example.aplapollo.helper.SessionManager
            import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
            import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
            import com.example.apolloapl.R
            import com.example.apolloapl.databinding.ActivitySlittingBinding
            import es.dmoral.toasty.Toasty

            class SlittingActivity : AppCompatActivity() {

                private lateinit var binding: ActivitySlittingBinding
                private lateinit var progress: ProgressDialog
                private lateinit var slittingViewModel: SlittingViewModel

                private lateinit var session: SessionManager
                private var userName: String? = ""
                private var token: String? = ""
                private var tenantCode: String? = ""

                private var locationId: Int = 0

                private lateinit var ongoingJobAdapter: OngoingJobAdapter
private var selectedLocationId:Int=0
                private var selectedProcessName: String = ""
                private var selectedMachineName: String = ""

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)

                    binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting)
                    supportActionBar?.hide()

                    binding.idLayoutHeader.tvTitle.text = "On Going Job"

                    progress = ProgressDialog(this)
                    progress.setMessage("Please Wait...")



                    binding.idLayoutHeader.ivBack.setOnClickListener {
                        onBackPressedDispatcher.onBackPressed()
                    }

                    session = SessionManager(this)
                    val userDetail = session.getUserDetails()

                    if (userDetail!!.isEmpty()) {
                        Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
                    } else {
                        token = userDetail["jwtToken"].toString()
                        userName = userDetail["userName"].toString()
                        tenantCode = userDetail["defaultTenantCode"].toString()

                        Log.d("JWT_TOKEN_QC", "JWT Token = $token")
                    }


                    val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
                    val factory = SlittingViewModelfactory(application, retrofitInstance)
                    slittingViewModel = ViewModelProvider(this, factory)[SlittingViewModel::class.java]
                    locationId = intent.getIntExtra(LocationId, -1)

                    Log.d("LOCATION_ID", "Received locationId = $locationId")
                    Log.d("Tenant", "Received tenant = $tenantCode")
                    if (locationId == -1) {
                        Toasty.error(this, "Invalid locationId").show()
                        return
                    }
                    selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
                    selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""
//                    slittingViewModel.getOngoingSlittingJobs(tenantCode.toString(),locationId)
                    ongoingJobAdapter = OngoingJobAdapter(emptyList()) { selectedJob ->

                        val intent = Intent(this, SlittingStatusActivity::class.java)

                        intent.putExtra(JobId, selectedJob.jobNumber)
                        intent.putExtra(HrSlittingPlanId, selectedJob.hrSlittingPlanId)
                        intent.putExtra(HrSlittingId, selectedJob.hrSlittingTranId)
                        intent.putExtra(SourceStockId, selectedJob.sourceStockId)
                        intent.putExtra(LocationId, selectedJob.locationId)

                        intent.putExtra(MotherWeightV, selectedJob.stockTransaction?.weight.toString())
                        intent.putExtra(BarcodeValue, selectedJob.stockTransaction?.barcode)
                        intent.putExtra(SupplierNo, selectedJob.stockTransaction?.supplierBatchNo)
                        intent.putExtra(WidthId, selectedJob.stockTransaction?.width)
                        intent.putExtra(ThicknessV, selectedJob.stockTransaction?.thickness)
                        intent.putExtra(GradeV, selectedJob.stockTransaction?.grade)

                        // ✅ Pass process & machine
                        intent.putExtra("PROCESS_NAME", selectedProcessName)
                        intent.putExtra("MACHINE_NAME", selectedMachineName)

                        startActivity(intent)
                    }

                    binding.rvOngoingJobs.apply {
                        layoutManager = LinearLayoutManager(this@SlittingActivity)
                        adapter = ongoingJobAdapter
                    }



                    slittingViewModel.ongoingJobsLiveData.observe(this) { resource ->
                        when (resource) {
                            is Resource.Loading -> progress.show()

                            is Resource.Success -> {
                                progress.dismiss()

                                val jobs = resource.data ?: emptyList()

                                Log.d("API_DEBUG", "Jobs size = ${jobs.size}")
                                Log.d("API_DEBUG", "Jobs data = $jobs")
                                if (jobs.isEmpty()) {
                                    Log.d("API_DEBUG", "Empty response - ignoring")
                                    return@observe   // ❗ prevent override
                                }
                                ongoingJobAdapter.updateList(jobs)
//                                if (jobs.isEmpty()) {
//                                    Toasty.info(this, "No ongoing jobs found").show()
//                                    ongoingJobAdapter.updateList(emptyList())
//                                } else {
//                                    ongoingJobAdapter.updateList(jobs)
//                                }
                            }

                            is Resource.Error -> {
                                progress.dismiss()
                                Toasty.error(this, resource.message ?: "Error loading jobs").show()
                            }

                            else -> {}
                        }
                    }


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
                                val intent = Intent(this, SlittingPlan2Activity::class.java)
                                intent.putExtra(LocationId, locationId)
                                startActivity(intent)
                            }

                            1 -> {
                                val intent = Intent(this, Slittingplan3Activity::class.java)
                                intent.putExtra(LocationId, locationId)
                                Log.d("LOCATION_ID", "Slittingplan3Activity locationId = $locationId")

                                startActivity(intent)
                            }
                        }
                    }
                }

                override fun onResume() {
                    super.onResume()
                    if (locationId != -1) {
                        slittingViewModel.getOngoingSlittingJobs(tenantCode.toString(),locationId)
                        Log.d("LOCATION_ID", "onResume locationId = $locationId")
                    }
                }
                }