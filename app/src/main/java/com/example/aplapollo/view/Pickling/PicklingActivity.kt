package com.example.aplapollo.view.Pickling

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
import com.example.aplapollo.adapter.Pickling.PicklingOngoingAdapter
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Constants.BarcodeValue
import com.example.aplapollo.helper.Constants.PicklingId
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.PrintLabelBarcodeRequest
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModel
import com.example.aplapollo.viewmodel.Pickling.PicklingViewModelfactory
import com.example.aplapollo.viewmodel.printlabel.PrintlabelViewModel
import com.example.aplapollo.viewmodel.printlabel.QcprintlabelViewModelFactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingBinding
import es.dmoral.toasty.Toasty

class PicklingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPicklingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var picklingViewModel: PicklingViewModel
    private lateinit var printlabelViewModel: PrintlabelViewModel
    private lateinit var ongoingJobAdapter:
            PicklingOngoingAdapter

    private var locationId: Int = 0

    private var selectedProcessName: String = ""
    private var selectedMachineName: String = ""

    private var userDetail:
            HashMap<String, Any?>? = null

    private var token: String? = ""
    private var userName: String? = ""
    private var tenantCode: String? = ""

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_pickling
        )

        supportActionBar?.hide()

        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        session = SessionManager(this)
        userDetail = session.getUserDetails()





        if (userDetail!!.isNotEmpty()) {

            token =
                userDetail!!["jwtToken"].toString()

            userName =
                userDetail!!["userName"].toString()

            tenantCode =
                userDetail!!["defaultTenantCode"]
                    .toString()
        }



        val retrofitInstance =
            RetrofitInstance.getInstance(
                applicationContext
            )

        val factory =
            PicklingViewModelfactory(
                application,
                retrofitInstance
            )

        picklingViewModel =
            ViewModelProvider(
                this,
                factory
            )[PicklingViewModel::class.java]

        val viewModelProviderFactorys = QcprintlabelViewModelFactory(application, retrofitInstance)
        printlabelViewModel =
            ViewModelProvider(this, viewModelProviderFactorys)[PrintlabelViewModel::class.java]

        locationId =
            intent.getIntExtra(
                Constants.LocationId,
                -1
            )

        selectedProcessName =
            intent.getStringExtra(
                "PROCESS_NAME"
            ) ?: ""

        selectedMachineName =
            intent.getStringExtra(
                "MACHINE_NAME"
            ) ?: ""

        binding.idLayoutHeader.tvTitle.text =
            "$selectedProcessName OnGoing Jobs"
        binding.idLayoutHeader.tvSubtitle.text="Manage active coils and production jobs"

        binding.idLayoutHeader.ivBack
            .setOnClickListener {

                onBackPressedDispatcher.onBackPressed()
            }


        ongoingJobAdapter =
            PicklingOngoingAdapter(

                mutableListOf(),

                // DELETE
                onDeleteClick = { selectedJob ->

                    AlertDialog.Builder(this)
                        .setTitle("Delete")
                        .setMessage(
                            "Are you sure want to delete?"
                        )
                        .setPositiveButton(
                            "Yes"
                        ) { _, _ ->

                            Log.d(
                                "DELETE_JOB",
                                selectedJob.picklingTranId.toString()
                            )

                            picklingViewModel
                                .fetchPicklingDelete(
                                    selectedJob.picklingTranId
                                )
                        }
                        .setNegativeButton(
                            "No"
                        ) { dialog, _ ->

                            dialog.dismiss()
                        }
                        .show()
                },

                // ITEM CLICK
                onItemClick = { selectedJob ->

                    val intent =
                        Intent(
                            this,
                            PicklingOutwardActivity::class.java
                        )

                    intent.putExtra(
                        PicklingId,
                        selectedJob.picklingTranId
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

                    val barcode = selectedJob.picklingJobDetailsResponses
                        ?:emptyList()


                    if (barcode.isEmpty()) {

                        Toasty.error(
                            this,
                            "No barcode available for printing",
                            Toasty.LENGTH_SHORT
                        ).show()

                        return@PicklingOngoingAdapter
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
                LinearLayoutManager(
                    this@PicklingActivity
                )

            adapter = ongoingJobAdapter
        }



        if (locationId != -1) {

            picklingViewModel
                .getOngoingPicklingJobs(locationId,selectedProcessName)

        } else {

            Toasty.error(
                this,
                "Invalid LocationId"
            ).show()
        }



        picklingViewModel
            .ongoingJobsLiveData
            .observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {

                        progress.show()
                    }

                    is Resource.Success -> {

                        progress.dismiss()

                        val jobs =
                            resource.data
                                ?: emptyList()

                        Log.d(
                            "ONGOING_JOBS",
                            "SIZE = ${jobs.size}"
                        )

                        ongoingJobAdapter
                            .updateList(jobs)
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

                        Toasty.error(
                            this,
                            resource.message
                                ?: "Error loading jobs",
                            Toasty.LENGTH_SHORT
                        ).show()
                    }

                    else -> {}
                }
            }
        picklingViewModel.picklingDeleteLiveData.observe(this) {

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

                    picklingViewModel.getOngoingPicklingJobs(
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
        binding.btnInProgress
            .setOnClickListener {

                val intent =
                    Intent(
                        this,
                        PicklingInwardActivity::class.java
                    )

                intent.putExtra(
                    Constants.LocationId,
                    locationId
                )

                intent.putExtra(
                    "PROCESS_NAME",
                    selectedProcessName
                )

                intent.putExtra(
                    "MACHINE_NAME",
                    selectedMachineName
                )

                startActivity(intent)
            }
    }

    override fun onResume() {

        super.onResume()

        if (locationId != -1) {

            picklingViewModel
                .getOngoingPicklingJobs(locationId,selectedProcessName)
        }
    }
}