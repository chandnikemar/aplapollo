package com.example.aplapollo.view.slitting

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Slitting.OngoingJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Constants.HrSlittingPlanId
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils.getCurrentDateTimeISO
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingBinding
import es.dmoral.toasty.Toasty
import java.net.URLDecoder

class SlittingActivity : AppCompatActivity() {

                private lateinit var binding: ActivitySlittingBinding
                private lateinit var progress: ProgressDialog
                private lateinit var slittingViewModel: SlittingViewModel
             private lateinit var printlabelViewModel: PrintlabelViewModel

                private lateinit var session: SessionManager
                private var userName: String? = ""
                private var token: String? = ""
                private var tenantCode: String? = ""

                private var locationId: Int = 0

                private lateinit var ongoingJobAdapter: OngoingJobAdapter

                    private var selectedProcessName: String = ""
                    private var selectedMachineName: String = ""

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)

                    binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting)
                    supportActionBar?.hide()



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
                    slittingViewModel =
                        ViewModelProvider(this, factory)[SlittingViewModel::class.java]
                    val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
                    printlabelViewModel =
                        ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]
                    locationId = intent.getIntExtra(LocationId, -1)

                    Log.d("LOCATION_ID", "Received locationId = $locationId")
                    Log.d("Tenant", "Received tenant = $tenantCode")
                    if (locationId == -1) {
                        Toasty.error(this, "Invalid locationId").show()
                        return
                    }
                    selectedProcessName = intent.getStringExtra("PROCESS_NAME") ?: ""
                    selectedMachineName = intent.getStringExtra("MACHINE_NAME") ?: ""

                    binding.idLayoutHeader.tvTitle.text = "$selectedProcessName Ongoing Jobs"
                    binding.idLayoutHeader.tvSubtitle.text="Manage active coils and production jobs"
                    selectedProcessName =
                        URLDecoder.decode(
                            selectedProcessName,
                            "UTF-8"
                        )

                    slittingViewModel.getOngoingSlittingJobs(
                        locationId,
                        selectedProcessName
                    )
                    ongoingJobAdapter =
                        OngoingJobAdapter(

                            mutableListOf(),

                            // CARD CLICK
                            onItemClick = { selectedJob ->

                                val intent =
                                    Intent(
                                        this,
                                        SlittingStatusActivity::class.java
                                    )

                                intent.putExtra(
                                    HrSlittingPlanId,
                                    selectedJob.hrSlittingTranId
                                )
                                intent.putExtra(BarcodeValue,selectedJob.barcode)
                                intent.putExtra(Constants.LocationId, locationId)
                                intent.putExtra("PROCESS_NAME", selectedProcessName)
                                intent.putExtra("MACHINE_NAME", selectedMachineName)
                                startActivity(intent)
                            },


                            onDeleteClick = { selectedJob ->

                                AlertDialog.Builder(this)
                                    .setTitle("Delete")
                                    .setMessage("Are you sure want to delete?")
                                    .setPositiveButton("Yes") { _, _ ->

                                        slittingViewModel
                                            .deleteSlittingTransaction(
                                                selectedJob.hrSlittingTranId
                                            )
                                    }
                                    .setNegativeButton("No") { dialog, _ ->

                                        dialog.dismiss()
                                    }
                                    .show()
                            },

                            onReprintClick = { selectedJob ->


                                val details =
                                    selectedJob.hRSlittingJobDetailsResponse ?: emptyList()


                                if (details.isEmpty()) {

                                    Toasty.error(
                                        this,
                                        "No barcode available for printing",
                                        Toasty.LENGTH_SHORT
                                    ).show()

                                    return@OngoingJobAdapter
                                }


                                val printRequestList = details.map {
                                    PrintLabelBarcodeRequest(
                                        barcode = it.barcode ?: "",
                                        locationId = locationId,
                                        createdDate = getCurrentDateTimeISO(),
                                        createdBy = userName ?: ""

                                    )
                                }

                                printlabelViewModel.printLabelBarcode(printRequestList)
                            }
                        )

                    binding.rvOngoingJobs.apply {
                        layoutManager = LinearLayoutManager(this@SlittingActivity)
                        adapter = ongoingJobAdapter
                    }



                    slittingViewModel.ongoingJobsLiveData.observe(this) { resource ->

                        when (resource) {

                            is Resource.Loading -> {
                                progress.show()
                            }

                            is Resource.Success -> {

                                progress.dismiss()

                                val jobs = resource.data ?: emptyList()

                                Log.d("API_DEBUG", "Jobs = $jobs")

                                ongoingJobAdapter.updateList(jobs)

                                if (jobs.isEmpty()) {

                                    binding.layoutEmptyState.visibility = View.VISIBLE
                                    binding.rvOngoingJobs.visibility = View.GONE
                                    binding.tvCoilCount.text = "0"

                                } else {

                                    binding.layoutEmptyState.visibility = View.GONE
                                    binding.rvOngoingJobs.visibility = View.VISIBLE
                                    binding.tvCoilCount.text = jobs.size.toString()
                                }
                            }

                            is Resource.Error -> {

                                progress.dismiss()

                                ongoingJobAdapter.updateList(emptyList())

                                binding.layoutEmptyState.visibility = View.VISIBLE
                                binding.rvOngoingJobs.visibility = View.GONE
                                binding.tvCoilCount.text = "0"

                                Toasty.error(
                                    this,
                                    resource.message ?: "Error loading jobs"
                                ).show()
                            }

                            else -> {}
                        }
                    }
                    slittingViewModel.deleteSlittingTranLiveData.observe(this) {

                        when (it) {

                            is Resource.Loading -> {

                                progress.show()
                            }

                            is Resource.Success -> {

                                progress.dismiss()

                                Toasty.success(
                                    this,
                                    it.data?.responseMessage ?: "Deleted"
                                ).show()

                                slittingViewModel.getOngoingSlittingJobs(
                                    locationId,selectedProcessName
                                )
                            }

                            is Resource.Error -> {

                                progress.dismiss()

                                Toasty.error(
                                    this,
                                    it.message ?: "Delete failed"
                                ).show()
                            }

                            else -> {}
                        }
                    }
                    printlabelViewModel.barcodePrintLabelMutableLiveData.observe(this) { resource ->

                        when (resource) {

                            is Resource.Loading -> {
                                progress.show()
                            }

                            is Resource.Success -> {

                                progress.dismiss()

                                Toasty.success(
                                    this,
                                    "Barcode printed successfully",
                                    Toasty.LENGTH_SHORT
                                ).show()

                                Log.d("PRINT_SUCCESS", resource.data.toString())

                                // OPTIONAL:
                                // Call physical printer here

//                                 finish()
                            }

                            is Resource.Error -> {

                                progress.dismiss()

                                Toasty.error(
                                    this,
                                    resource.message ?: "Print failed",
                                    Toasty.LENGTH_SHORT
                                ).show()

                                resource.message?.let {
                                    Log.e(
                                        "PRINT_API_ERROR",
                                        it
                                    )
                                }
                            }


                        }
                    }
                    binding.btnInProgress.setOnClickListener {
                        val intent = Intent(this, Slittingplan3Activity::class.java)
                        intent.putExtra(Constants.LocationId, locationId)
                        intent.putExtra("PROCESS_NAME", selectedProcessName)
                        intent.putExtra("MACHINE_NAME", selectedMachineName)

                        startActivity(intent)
                    }
                }


                override fun onResume() {
                    super.onResume()
                    if (locationId != -1) {
                        slittingViewModel.getOngoingSlittingJobs(locationId,selectedProcessName)
                        Log.d("LOCATION_ID", "onResume locationId = $locationId")
                    }
                }
                }