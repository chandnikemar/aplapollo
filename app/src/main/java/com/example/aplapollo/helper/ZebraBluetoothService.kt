package com.example.aplapollo.helper

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.aplapollo.service.ZebraPrintService

object ZebraPrinterHelper {

    private const val TAG = "ZEBRA_PRINT"
    private val mainHandler = Handler(Looper.getMainLooper())

    /** Public method to print ZPL via Service */
    fun printViaService(context: Context, mac: String?, zpl: String) {
        if (mac.isNullOrEmpty()) {
            showToast(context, "Printer MAC not configured")
            return
        }

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            showToast(context, "Bluetooth not supported")
            return
        }

        if (!adapter.isEnabled) {
            showToast(context, "Bluetooth disabled")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showToast(context, "Bluetooth permission missing")
            return
        }

        val intent = Intent(context, ZebraPrintService::class.java).apply {
            action = ZebraPrintService.ACTION_PRINT
            putExtra(ZebraPrintService.EXTRA_MAC, mac)
            putExtra(ZebraPrintService.EXTRA_ZPL, zpl)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /** Show Toast on main thread */
    private fun showToast(context: Context, message: String) {
        mainHandler.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    /** Check if printer is paired & available */
    fun isPrinterAvailable(context: Context, printerMac: String?): Boolean {
        if (printerMac.isNullOrEmpty()) return false
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return false
        if (!adapter.isEnabled) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return adapter.bondedDevices.any { it.address.equals(printerMac, ignoreCase = true) }
    }
}
