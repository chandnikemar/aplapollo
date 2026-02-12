package com.example.aplapollo.view.coldpressing

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityCrmtransactionBinding

class CRMTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrmtransactionBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crmtransaction)
        binding.idLayoutHeader.tvTitle.text = "CRN Transaction"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
    }
}