package com.example.aplapollo.view

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aplapollo.adapter.OngoingJobAdapter
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.model.Slitting.OngoingJob
import com.example.aplapollo.view.Slitting.SlittingPlan2Activity
import com.example.aplapollo.view.Slitting.Slittingplan3Activity
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivitySlittingBinding

class SlittingActivity : AppCompatActivity() {
        private lateinit var binding: ActivitySlittingBinding
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private var baseUrl: String = ""
    private var userName: String? = ""
    private var token: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_slitting)
        binding.idLayoutHeader.tvTitle.text = "On Going Job"


        binding.idLayoutHeader.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        supportActionBar?.hide()
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val ongoingJobs = listOf(
            OngoingJob("B34R2642644", 12, "5.0 mm", "S355" ),
            OngoingJob("C-00126", 10, "6.0 mm", "S275"),
            OngoingJob("C-00126", 10, "6.0 mm", "S275" ),
            OngoingJob("C-00126", 10, "6.0 mm", "S275")
        )

        val adapter = OngoingJobAdapter(ongoingJobs) { selectedJob ->
            // Only FIRST item click will come here
            val intent = Intent(this, SlittingStatusActivity::class.java)

            // Optional: pass data
            intent.putExtra("BARCODE", selectedJob.barcode)
            intent.putExtra("WIDTH", selectedJob.width)
            intent.putExtra("THICKNESS", selectedJob.thickness)
            intent.putExtra("GRADE", selectedJob.grade)

            startActivity(intent)
        }
        binding.rvOngoingJobs.layoutManager = LinearLayoutManager(this)
        binding.rvOngoingJobs.adapter = adapter
        val coilOptions = listOf(
            "Select from plan",
            "Without plan",

        )
        val coilAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            coilOptions
        )
        binding.ddAddNewCoil.setAdapter(coilAdapter)
        binding.ddAddNewCoil.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> { /* Add manually */
                    val intent = Intent(this, SlittingPlan2Activity::class.java)
                    startActivity(intent)}
                1 -> { /* Scan coil */
                    val intent = Intent(this, Slittingplan3Activity::class.java)
                    startActivity(intent)}

            }

        }
       binding.tilAddNewCoil.setEndIconTintList(
            ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white))
        )

    }
}