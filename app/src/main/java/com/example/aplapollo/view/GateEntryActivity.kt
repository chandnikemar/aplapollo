package com.example.aplapollo.view

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityGateEntryBinding
import es.dmoral.toasty.Toasty

class GateEntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGateEntryBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var tenantCode: String
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""

    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gate_entry)
        binding.idLayoutHeader.tvTitle.text = "Gate Entry"

        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val headerBinding = binding.idLayoutHeader

        headerBinding.printerStatusContainer.visibility = View.VISIBLE
        headerBinding.ivPrinter.setImageResource(R.drawable.printer_white_on)
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
        val retrofitInstance =
            RetrofitInstance.getInstance(applicationContext)
        session = SessionManager(this)
        userDetail = session.getUserDetails()
        if (userDetail!!.isEmpty()) {
            Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
        } else {

            token = userDetail!!["jwtToken"].toString()
            userName = userDetail!!["userName"].toString()
            tenantCode = userDetail!![SessionManager.Key_tenantCode]?.toString() ?: ""


            serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
            serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()

            baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"

            Log.d("SESSION_DEBUG", "User Details = $userDetail")

            // ⭐ PRINT TOKEN HERE
            Log.d("JWT_TOKEN_QC", "JWT Token = $token")
            Log.d("Tanent_Code","Tenant Code= $tenantCode")
        }


        binding.btnClear.setOnClickListener {

            binding.etTransporter.text?.clear()
            binding.etVehicleNo.text?.clear()

            binding.etCoilNo.text?.clear()
        }

    }
}