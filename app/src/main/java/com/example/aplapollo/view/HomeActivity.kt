package com.example.aplapollo.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.ZebraPrinterHelper
import com.example.aplapollo.view.GP.GPActivity
import com.example.aplapollo.view.Pickling.PicklingActivity
import com.example.aplapollo.view.coldpressing.CRMActivity
import com.example.aplapollo.view.slitting.SlittingActivity
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    private var userDetails: HashMap<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        session = SessionManager(this)
        val username = Utils.getSharedPrefs(this, Constants.KEY_USER_NAME)
        binding.idLayoutHeader.profileTXt.text = username
        updatePrinterIndicator()
        val summaryList = listOf(
            SummaryItem(
                "Gate Entry",
                12,
                R.drawable.ic_dashboard,
                Color.parseColor("#3A6C97"),
                Color.parseColor("#6EC6FF")
            ),
            SummaryItem(
                "Quality Check",
                5,
                R.drawable.ic_dashboard,
                Color.parseColor("#11998E"),
                Color.parseColor("#38EF7D")
            ),
            SummaryItem(
                "HR Slitting",
                8,
                R.drawable.ic_dashboard,
                Color.parseColor("#FC466B"),
                Color.parseColor("#3F5EFB")
            ),
            SummaryItem(
                "Dispatch",
                3,
                R.drawable.ic_dashboard,
                Color.parseColor("#F7971E"),
                Color.parseColor("#FFD200")

            )
        )
//        binding.rvSummary.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        binding.rvSummary.adapter = SummaryAdapter(summaryList)


        binding.card1.setOnClickListener {
            startActivity(Intent(this@HomeActivity, GateEntryActivity::class.java))
        }
        binding.card2.setOnClickListener {
            startActivity(Intent(this@HomeActivity, QualityCheckActivity::class.java))
        }
        binding.card3.setOnClickListener {
            startActivity(Intent(this@HomeActivity, SlittingActivity::class.java))
        }
        binding.card4.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PicklingActivity ::class.java))
        }
        binding.card5.setOnClickListener {
            startActivity(Intent(this@HomeActivity, CRMActivity ::class.java))
        }
        binding.card6.setOnClickListener {
            startActivity(Intent(this@HomeActivity, GPActivity ::class.java))
        }
        binding.card7.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PrinterMACAddActivity::class.java))
        }
        binding.card8.setOnClickListener {
            startActivity(Intent(this@HomeActivity, AdminActivity::class.java))
        }
        binding.idLayoutHeader.ivPrinter.setOnClickListener {

            val printerMac = Utils.getSharedPrefs(this, Constants.KEY_PRINTER_MAC)
            Log.d("ZEBRA_PRINT", "Stored MAC = $printerMac")

            if (printerMac.isNullOrEmpty()) {
                Toast.makeText(this, "Printer not configured", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val zpl = """
        ^XA
        ^FO50,50^A0N,50,50^FDHello World Chandni^FS
        ^XZ
    """.trimIndent()

            ZebraPrinterHelper.printViaService( context = this, mac = printerMac, zpl=zpl)
        }

        // Logout
        binding.idLayoutHeader.logouticon.setOnClickListener {
            showLogoutPopup()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePrinterIndicator()
    }
    private fun updatePrinterIndicator() {
        val indicator = binding.idLayoutHeader.viewPrinterStatus
        val printerMac = Utils.getSharedPrefs(this, Constants.KEY_PRINTER_MAC)

        val isAvailable = ZebraPrinterHelper.isPrinterAvailable(this, printerMac)

        indicator.setBackgroundResource(
            if (isAvailable)
                R.drawable.bg_status_green
            else
                R.drawable.bg_status_red
        )
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
        session.logoutKeepAdminConfig()
        startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
        finish()
    }

}

data class SummaryItem(
    val title: String,
    val count: Int,
    val icon: Int,
    val startColor: Int,
    val endColor: Int
)