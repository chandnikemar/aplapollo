package com.example.aplapollo.view.coldpressing

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
import com.example.aplapollo.adapter.Coldpressing.OngoingCRMJobAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.CrmTranJob
import com.example.aplapollo.helper.Constants.LocationId
import com.example.aplapollo.helper.Constants.LocationName
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.crm.CRMViewModel
import com.example.aplapollo.viewmodel.crm.CRMViewModelfactory
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmactivityBinding
import es.dmoral.toasty.Toasty

class CRMActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrmactivityBinding
    private lateinit var progress: ProgressDialog
    private lateinit var crmViewModel: CRMViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private lateinit var session: SessionManager

    private var userName: String? = ""
    private var tenantCode: String? = ""

    // ✅ FIXED LOCATION (CHANGE IF NEEDED)
    private var locationId: Int = 1
    private var locationName: String = "Default Location"

    private lateinit var ongoingJobAdapter: OngoingCRMJobAdapter
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    private var headerTittle:String=""
    private var completeTittle:String=""
    private var headerTittleCRCA:String=""
    private var completeTittleCRCA:String=""
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmactivity)

        supportActionBar?.hide()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val retrofitInstance = RetrofitInstance.getInstance(applicationContext)
        val factory = CRMViewModelfactory(application, retrofitInstance)
        crmViewModel = ViewModelProvider(this, factory)[CRMViewModel::class.java]
        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]

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
        headerTittle=intent.getStringExtra("FIRST_PAGECRFH")?:""
        completeTittle=intent.getStringExtra("Completed_PAGECRFH")?:""
        headerTittleCRCA=intent.getStringExtra("FIRST_PAGECRCA")?:""
completeTittleCRCA=intent.getStringExtra("Completed_PAGECRCA")?:""
        binding.idLayoutHeader.tvTitle.text = "$selectedProcessName OnGoing Jobs"
        binding.idLayoutHeader.tvSubtitle.text="Manage active coils and production jobs"
        Log.d("LOCATION_ID", "Received locationId = $locationId")

        if (locationId != -1) {
            crmViewModel.getOngoingCRMJobs(locationId,selectedProcessName)

        } else {
            Toasty.error(this, "Invalid LocationId").show()
        }
        ongoingJobAdapter = OngoingCRMJobAdapter(

            jobList = mutableListOf(),

            // =========================
            // ITEM CLICK
            // =========================
            onItemClick = { selectedJob ->

                val intent =
                    Intent(
                        this,
                        CRMTransactionActivity::class.java
                    )

                intent.putExtra(
                    LocationId,
                    locationId
                )

                intent.putExtra(
                    LocationName,
                    locationName
                )

                intent.putExtra(
                    CrmTranJob,
                    selectedJob.crmTranId
                )

                intent.putExtra(
                    "GRADE",
                    selectedJob.grade
                )

                intent.putExtra(
                    "PROCESS_NAME",
                    selectedProcessName
                )

                intent.putExtra(
                    "MACHINE_NAME",
                    selectedMachineName
                )

                intent.putExtra(
                    "Completed_PAGECRFH",
                    completeTittle
                )

                startActivity(intent)
            },


            onDeleteClick = { selectedJob ->

                AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Are you sure want to delete?")
                    .setPositiveButton("Yes") { _, _ ->


                         crmViewModel.fetchCRMDelete(
                             selectedJob.crmTranId
                         )
                    }
                    .setNegativeButton("No") { dialog, _ ->

                        dialog.dismiss()
                    }
                    .show()
            },

            onReprintClick = { selectedJob ->

                val barcode = selectedJob.crmJobDetailsResponse
                    ?:emptyList()


                if (barcode.isEmpty()) {

                    Toasty.error(
                        this,
                        "No barcode available for printing",
                        Toasty.LENGTH_SHORT
                    ).show()

                    return@OngoingCRMJobAdapter
                }

                val printRequestList = barcode.map {
                    PrintLabelBarcodeRequest(
                        barcode = it.barcode ?: "",
                        locationId = locationId,
                        createdDate = Utils.getCurrentDateTimeISO(),
                        createdBy = userName ?: ""

                    )
                }

                printlabelViewModel.printLabelBarcode(printRequestList)
            }
        )
        binding.rvOngoingJobs.apply {

            layoutManager =
                LinearLayoutManager(this@CRMActivity)

            adapter = ongoingJobAdapter
        }

        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        binding.rvOngoingJobs.adapter = ongoingJobAdapter

        // ---------------- Load Jobs ----------------
        crmViewModel.getOngoingCRMJobs(locationId,selectedProcessName)

        crmViewModel.ongoingJobsLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> progress.show()

                is Resource.Success -> {
                    progress.dismiss()

                    val jobs = resource.data ?: emptyList()

                    if (jobs.isEmpty()) {
//                        Toasty.info(this, "No ongoing jobs found").show()
                        ongoingJobAdapter.updateList(emptyList())
                    } else {
                        ongoingJobAdapter.updateList(jobs)
                    }
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

                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvOngoingJobs.visibility = View.GONE
                    binding.tvCoilCount.text = "0"
                    Toasty.error(this, resource.message ?: "Error").show()
                }

                else -> {}
            }
        }
        printlabelViewModel.barcodePrintLabelMutableLiveData.observe(this){ resource->
            when (resource) {
                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()

                    finish()
                }

                is Resource.Error -> {
                    progress.dismiss()

                }

                else -> {}
            }}
        crmViewModel.crmDeleteLiveData.observe(this) {

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

                    crmViewModel.getOngoingCRMJobs(
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
        binding.btnInProgress.setOnClickListener {
            val intent = Intent(this, CRMPlanOutwardActivity::class.java)
            intent.putExtra(LocationId, locationId)
            intent.putExtra("PROCESS_NAME", selectedProcessName)
            intent.putExtra("MACHINE_NAME", selectedMachineName)
            intent.putExtra("FIRST_PAGECRFH", headerTittle)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()
        crmViewModel.getOngoingCRMJobs(locationId,selectedProcessName)
    }
}
