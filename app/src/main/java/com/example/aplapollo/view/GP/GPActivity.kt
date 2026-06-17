package com.example.aplapollo.view.GP

import android.annotation.SuppressLint
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
import com.example.aplapollo.adapter.Gp.OngoingGpAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.GP.GpOngoingJobsResponse
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.GP.GpViewModel
import com.example.aplapollo.viewmodel.GP.GpViewModelFactory
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGpactivityBinding
import es.dmoral.toasty.Toasty

class GPActivity : AppCompatActivity() {
    private lateinit var binding:ActivityGpactivityBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager

    private  lateinit var  gpViewModel: GpViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var locationId: Int = 0

    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null

    private lateinit var ongoingJobAdapter: OngoingGpAdapter
    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""
    private var pendingDeleteJob: GpOngoingJobsResponse? = null
    private var pendingDeleteId = -1


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gpactivity)

        supportActionBar?.hide()

        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        session = SessionManager(this)
        userDetail = session.getUserDetails()

        val viewModelProviderFactoryGp = GpViewModelFactory(application, retrofitInstance)
        gpViewModel = ViewModelProvider(this, viewModelProviderFactoryGp)[GpViewModel::class.java]
        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]
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
        binding.idLayoutHeader.tvTitle.text = "$selectedProcessName OnGoing Jobs"
        binding.idLayoutHeader.tvSubtitle.text="Manage active coils and production jobs"

        if (locationId != -1) {
            gpViewModel.getOngoingGpJobs(locationId,selectedProcessName)

        } else {
            Toasty.error(this, "Invalid LocationId").show()
        }
        ongoingJobAdapter = OngoingGpAdapter(

            mutableListOf(),
            onDeleteClick = { selectedJob ->
                pendingDeleteId = selectedJob.galvanizingTranId
                Log.d(
                    "DELETE_CLICK",
                    "Selected ID = $pendingDeleteId"
                )
                AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Are you sure want to delete?")
                    .setPositiveButton("Yes") { _, _ ->

                gpViewModel.fetchGpDelete(
                 pendingDeleteId
                )
                    }
                    .setNegativeButton("No") { dialog, _ ->

                        dialog.dismiss()
                    }
                    .show()
            },



            onItemClick = { selectedJob ->

                val intent = Intent(
                    this,
                    GPOutwardActivity::class.java
                )

                intent.putExtra(
                    "GPTranId",
                    selectedJob.galvanizingTranId
                )

                intent.putExtra(
                    BarcodeValue,
                    selectedJob.barcode
                )

                intent.putExtra(
                    "THICKNESS",
                    selectedJob.thickness
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
                    Constants.LocationId,
                    locationId
                )

                startActivity(intent)
            },

            // REPRINT
            onReprintClick = { selectedJob ->

                val barcode = selectedJob.galvanizingJobDetailsResponse
                    ?:emptyList()
                if (barcode.isEmpty()) {

                    Toasty.error(
                        this,
                        "No barcode available for printing",
                        Toasty.LENGTH_SHORT
                    ).show()

                    return@OngoingGpAdapter
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
                LinearLayoutManager(this@GPActivity)

            adapter =
                ongoingJobAdapter
        }

        binding.rvOngoingJobs.apply {
            layoutManager = LinearLayoutManager(this@GPActivity)
            adapter = ongoingJobAdapter
        }

        binding.btnInProgress.setOnClickListener {
            val intent = Intent(this, GPInwardActivity::class.java)
            intent.putExtra(Constants.LocationId, locationId)
            intent.putExtra("PROCESS_NAME", selectedProcessName)
            intent.putExtra("MACHINE_NAME", selectedMachineName)
            startActivity(intent)
        }

        gpViewModel.ongoingGpJobsLiveData.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> progress.show()
                is Resource.Success -> {

                    progress.dismiss()

                    val jobs = resource.data ?: emptyList()

                    Log.d("GP_REFRESH", "Jobs Count = ${jobs.size}")
                    jobs.forEach {
                        Log.d(
                            "REFRESH_DATA",
                            "TranId=${it.galvanizingTranId}, Barcode=${it.barcode}"
                        )
                    }
                    ongoingJobAdapter.updateList(jobs)

                    binding.tvCoilCount.text = jobs.size.toString()

                    if (jobs.isEmpty()) {

                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvOngoingJobs.visibility = View.GONE

                    } else {

                        binding.layoutEmptyState.visibility = View.GONE
                        binding.rvOngoingJobs.visibility = View.VISIBLE
                    }
                }
                is Resource.Error -> {
                    progress.dismiss()
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvOngoingJobs.visibility = View.GONE
                    binding.tvCoilCount.text = "0"
                    Toasty.error(this, resource.message ?: "Error loading jobs", Toasty.LENGTH_SHORT).show()
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

                    // finish()
                }

                is Resource.Error -> {

                    progress.dismiss()

                    Toasty.error(
                        this,
                        resource.message ?: "Print failed",
                        Toasty.LENGTH_SHORT
                    ).show()

                    Log.e("PRINT_ERROR", resource.message ?: "")
                }

                else -> {}
            }
        }
        gpViewModel.gpDeleteLiveData.observe(this) {

            when (it) {

                is Resource.Loading -> {

                    progress.show()
                }

                is Resource.Success -> {

                    progress.dismiss()

                    ongoingJobAdapter.removeItem(pendingDeleteId)

                    binding.tvCoilCount.text =
                        ongoingJobAdapter.itemCount.toString()

                    if (ongoingJobAdapter.itemCount == 0) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.rvOngoingJobs.visibility = View.GONE
                    }

                    Toasty.success(
                        this,
                        it.data?.responseMessage ?: "Deleted"
                    ).show()

                    gpViewModel.getOngoingGpJobs(
                        locationId,
                        selectedProcessName
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

    }

    override fun onResume() {
        super.onResume()
        if (locationId != -1) {
            gpViewModel.getOngoingGpJobs(locationId, selectedProcessName )
        }
    }
}