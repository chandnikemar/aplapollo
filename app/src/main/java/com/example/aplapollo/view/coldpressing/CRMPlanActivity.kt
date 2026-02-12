package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmplanBinding

class CRMPlanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmplanBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmplan    )
        binding.idLayoutHeader.tvTitle.text = "CRM Plan"
        supportActionBar?.hide()
        progress = ProgressDialog(this)

        binding.btncSaves.setOnClickListener {
            startActivity(Intent(this@CRMPlanActivity, CRMTransactionActivity::class.java))
        }
    }
}