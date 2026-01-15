package com.example.aplapollo.view.Slitting

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.Slitting.SlittingWidthAdapter
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Resource
import com.example.aplapollo.helper.SessionExpiredEvent
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.Slitting.HrSlittingPlanDetail
import com.example.aplapollo.model.Slitting.HrSlittingPlanResponse
import com.example.aplapollo.repository.APLRepository
import com.example.aplapollo.view.LoginActivity
import com.example.aplapollo.viewmodel.slitting.SlittingViewModel
import com.example.aplapollo.viewmodel.slitting.SlittingViewModelfactory
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingPlan2Binding
import es.dmoral.toasty.Toasty

class SlittingPlan2Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySlittingPlan2Binding
    private lateinit var slittingViewModel: SlittingViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    private lateinit var planAdapter: ArrayAdapter<String>
    private var planList = listOf<HrSlittingPlanResponse>()
    private lateinit var slittingWidthAdapter: SlittingWidthAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this
                ,R.layout.activity_slitting_plan2)
        binding.idLayoutHeader.tvTitle.text = "Slitting From Plan "
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val aplRepository = APLRepository()
        val viewModelProviderFactory = SlittingViewModelfactory(application, aplRepository)
        slittingViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingViewModel::class.java]
        slittingWidthAdapter = SlittingWidthAdapter()
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


            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }
        SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
            if (shouldLogout == true) {
                SessionExpiredEvent.logoutLiveData.value = false
                showLogoutPopup()
            }
        }
        slittingViewModel.getHrSlittingPlannedList(baseUrl)

        slittingViewModel.hrSlittingPlanMutableLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    // show progress
                }

                is Resource.Success -> {
                    resource.data?.let { list ->
                        planList = list
                        setupPlanDropdown(list)
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
        slittingViewModel.hrSlittingPlanDetailLiveData.observe(this) { resource ->
            when (resource) {

                is Resource.Loading -> {
                    progress.show()
                }

                is Resource.Success -> {
                    progress.dismiss()
                    resource.data?.let { planDetail ->

                        val widths = resource.data?.hrSlittingPlanDetail
                            ?.map { it.requiredCoilWidth }
                            ?: emptyList()
                        // Show the sections
                        binding.layoutInputRequirement.visibility = android.view.View.VISIBLE
                        binding.layoutSlittingPlan.visibility = android.view.View.VISIBLE
                        binding.layoutScanCoil.visibility = android.view.View.VISIBLE
                        binding.layoutButtons.visibility = android.view.View.VISIBLE

                        // Bind the data
                        binding.Tvitem.text = "Item: ${planDetail.materialCode}"
                        binding.tvGrade.text = "Grade: ${planDetail.grade}"
                        binding.tvWidth.text = "Width: ${planDetail.width} MM"
                        binding.tvthickness.text = "Thickness: ${planDetail.thickness} MM"

                        // Bind TableLayout for hrSlittingPlanDetail
                        setupSlittingPlanTable(planDetail.hrSlittingPlanDetail)
                        binding.rvSlittingWidths.apply {
                            adapter = slittingWidthAdapter
                            layoutManager = LinearLayoutManager(
                                context,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                        }
                        slittingWidthAdapter.submitList(widths)
                    }
                }

                is Resource.Error -> {
                    progress.dismiss()
                    Toasty.error(this, resource.message ?: "Failed to load plan details", Toasty.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }


        binding.tilSelectPlan.defaultHintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))

        binding.tilSelectPlan.hintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))

        binding.tilSelectPlan.setEndIconTintList(
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        )


        binding.spinnerSelectPlan.setOnItemClickListener { _, _, position, _ ->

            val selectedPlanNo = planAdapter.getItem(position)

            val selectedPlan = planList.firstOrNull {
                it.hrSlittingPlanNo == selectedPlanNo
            }

            selectedPlan?.let { plan ->

//                binding.Tvitem.text =
//                    "Item: ${plan.materialCode}"
//
//                binding.tvGrade.text =
//                    "Grade: ${plan.grade}"
//
//                binding.tvWidth.text =
//                    "Width: ${plan.width} MM"
//
//                binding.tvthickness.text =
//                    "Thickness: ${plan.thickness} MM"

                slittingViewModel.getHrSlittingPlanById(baseUrl,selectedPlan.hrSlittingPlanId)
            }

        }



    }
    private fun setupSlittingPlanTable(details: List<HrSlittingPlanDetail>) {
        binding.tableBatchDetailsTable.removeAllViews() // clear previous

        details.forEachIndexed { index, detail ->
            val tableRow = TableRow(this)

            val coilWidthText = TextView(this).apply {
                text = "Width: ${detail.requiredCoilWidth}"
                setPadding(8, 8, 8, 8)
            }

            val statusText = TextView(this).apply {
                text = "Active: ${detail.isActive}"
                setPadding(8, 8, 8, 8)
            }

            tableRow.addView(coilWidthText)
            tableRow.addView(statusText)
            binding.tableBatchDetailsTable.addView(tableRow)
        }
    }

    private fun setupPlanDropdown(list: List<HrSlittingPlanResponse>) {

        val planNumbers = list.map { it.hrSlittingPlanNo }

        planAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            planNumbers
        )

        binding.spinnerSelectPlan.setAdapter(planAdapter)
        binding.spinnerSelectPlan.setText("-- Select Plan --", false)
        binding.spinnerSelectPlan.keyListener = null
    }

    private fun showLogoutPopup() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun logout() {
        session.logoutUser()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}