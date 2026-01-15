package com.example.aplapollo.view

import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.SessionManager
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingStatusBinding

class SlittingStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySlittingStatusBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting_status)
        binding.idLayoutHeader.tvTitle.text = "Slitting Status"
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")
    }
}