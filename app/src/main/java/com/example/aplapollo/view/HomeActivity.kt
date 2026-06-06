package com.example.aplapollo.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.helper.Utils.getWifiMacAddress
import com.example.aplapollo.view.GateEntry.GateEntryTransactionActivity
import com.example.aplapollo.view.ProductionEntry.InputProductionEntryActivity
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityHomeBinding
import java.net.NetworkInterface
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var session: SessionManager
    private var userDetails: HashMap<String, Any?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        session = SessionManager(this)
        val username = Utils.getSharedPrefs(this, Constants.KEY_USER_NAME)
        binding.profileTXt.text = username
        NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->

            val mac = intf.hardwareAddress?.joinToString(":") {
                "%02X".format(it)
            } ?: "N/A"

            Log.d("NETWORK", "${intf.name} -> $mac")
        }
        Log.d("TC26_MAC", getWifiMacAddress())
        val deviceId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        Log.d("DEVICE_ID", deviceId)
        printDeviceInfo()
//        if (username != null) {
//            binding.tvAvatar.text =
//                username.trim().first().uppercaseChar().toString()
//        }
//        binding.idLayoutHeader..visibility=View.GONE
//        updatePrinterIndicator()

//        binding.rvSummary.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
//        binding.rvSummary.adapter = SummaryAdapter(summaryList)


        binding.cardGateEntry.setOnClickListener {
            startActivity(Intent(this@HomeActivity, GateEntryTransactionActivity::class.java))
        }
        binding.cardQuality.setOnClickListener {
            startActivity(Intent(this@HomeActivity, QualityCheckHistoryActivity::class.java))
        }

//        }
        binding.cardProduction.setOnClickListener {
            startActivity(Intent(this@HomeActivity, InputProductionEntryActivity::class.java))
        }
        binding.cardPrinter.setOnClickListener {
            startActivity(Intent(this@HomeActivity, PrinterMACAddActivity::class.java))
        }
        binding.cardAdmin.setOnClickListener {
            startActivity(Intent(this@HomeActivity, AdminActivity::class.java))
        }

//        binding.idLayoutHeader.ivPrinter.setOnClickListener {
//
//            val printerMac = Utils.getSharedPrefs(this, Constants.KEY_PRINTER_MAC)
//            Log.d("ZEBRA_PRINT", "Stored MAC = $printerMac")
//
//            if (printerMac.isNullOrEmpty()) {
//                Toast.makeText(this, "Printer not configured", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            val zpl = """
//        ^XA
//        ^FO50,50^A0N,50,50^FDHello World Chandni^FS
//        ^XZ
//    """.trimIndent()
//
//            ZebraPrinterHelper.printViaService( context = this, mac = printerMac, zpl=zpl)
//        }

        // Logout
        binding.logouticon.setOnClickListener {
            showLogoutPopup()
        }
    }

//    override fun onResume() {
//        super.onResume()
//        updatePrinterIndicator()
//    }
//    private fun updatePrinterIndicator() {
//        val indicator = binding.idLayoutHeader.viewPrinterStatus
//        val printerMac = Utils.getSharedPrefs(this, Constants.KEY_PRINTER_MAC)
//
//        val isAvailable = ZebraPrinterHelper.isPrinterAvailable(this, printerMac)
//
//        indicator.setBackgroundResource(
//            if (isAvailable)
//                R.drawable.bg_status_green
//            else
//                R.drawable.bg_status_red
//        )
//    }


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


    fun printDeviceInfo() {

        val androidId = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )

        Log.d("DEVICE_INFO", "ANDROID_ID = $androidId")

        Log.d("DEVICE_INFO", "MANUFACTURER = ${Build.MANUFACTURER}")
        Log.d("DEVICE_INFO", "MODEL = ${Build.MODEL}")
        Log.d("DEVICE_INFO", "DEVICE = ${Build.DEVICE}")
        Log.d("DEVICE_INFO", "PRODUCT = ${Build.PRODUCT}")
        Log.d("DEVICE_INFO", "BRAND = ${Build.BRAND}")

        try {
                NetworkInterface.getNetworkInterfaces().toList().forEach { intf ->

                    val mac = intf.hardwareAddress?.joinToString(":") {
                        "%02X".format(it)
                    } ?: "N/A"

                    Log.d(
                        "DEVICE_INFO",
                        "Interface=${intf.name}, MAC=$mac"
                    )
                }
        } catch (e: Exception) {
            Log.e("DEVICE_INFO", e.message ?: "")
        }
    }
}

//data class SummaryItem(
//    val title: String,
//    val count: Int,
//    val icon: Int,
//    val startColor: Int,
//    val endColor: Int
//)