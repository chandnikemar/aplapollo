package com.example.aplapollo.view.Pickling

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.api.RetrofitInstance
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityPicklingBinding
import es.dmoral.toasty.Toasty

class PicklingActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPicklingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""
    private  var tenantCode:String?=""
    private  var userDetail: HashMap<String, Any?>?=null
    private var serverIpSharedPrefText: String? = null
    private var serverHttpPrefText: String? = null
            override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                binding = DataBindingUtil.setContentView(this, R.layout.activity_pickling)
                binding.idLayoutHeader.tvTitle.text = "Hr Pickling"
                supportActionBar?.hide()
                progress = ProgressDialog(this)
                progress.setMessage("Please Wait...")
                val retrofitInstance =
                    RetrofitInstance.getInstance(applicationContext)
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



                supportActionBar?.hide()
                progress = ProgressDialog(this)
                progress.setMessage("Please Wait...")
    }
}