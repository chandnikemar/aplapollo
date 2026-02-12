package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmactivityBinding

class CRMActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmactivityBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmactivity)
        binding.idLayoutHeader.tvTitle.text = "Cold Pressing"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        binding.btnInProgress.setOnClickListener {
            startActivity(Intent(this@CRMActivity, CRMPlanActivity::class.java))
        }
    }
}