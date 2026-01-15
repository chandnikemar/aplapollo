package com.example.aplapollo.view.Slitting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingplan3Binding

class Slittingplan3Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySlittingplan3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this
            ,R.layout.activity_slittingplan3)
        binding.idLayoutHeader.tvTitle.text = "WithOut Plan "
        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}