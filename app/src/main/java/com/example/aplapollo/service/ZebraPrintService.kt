package com.example.aplapollo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.apolloapl.R
import com.zebra.sdk.comm.BluetoothConnection
import com.zebra.sdk.comm.Connection

class ZebraPrintService : Service() {

    companion object {
        const val ACTION_PRINT = "ACTION_PRINT"
        const val EXTRA_MAC = "EXTRA_MAC"
        const val EXTRA_ZPL = "EXTRA_ZPL"
        private const val CHANNEL_ID = "ZEBRA_PRINT_CHANNEL"
        private const val NOTIF_ID = 101
        private const val TAG = "ZEBRA_PRINT"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🟢 Service created")
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Printer service running"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "➡ onStartCommand called")

        if (intent?.action == ACTION_PRINT) {
            val mac = intent.getStringExtra(EXTRA_MAC)
            val zpl = intent.getStringExtra(EXTRA_ZPL)

            Log.d(TAG, "Printer MAC = $mac")
            Log.d(TAG, "ZPL length = ${zpl?.length}")

            if (!mac.isNullOrEmpty() && !zpl.isNullOrEmpty()) {
                print(mac, zpl)
            } else {
                Log.e(TAG, "❌ MAC or ZPL is empty")
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun print(printerMac: String, zpl: String) {
        Thread {
            var connection: Connection? = null

            try {
                Log.d(TAG, "🔵 Starting print thread")

                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter == null) {
                    Log.e(TAG, "❌ Bluetooth not supported")
                    stopSelf()
                    return@Thread
                }

                if (!adapter.isEnabled) {
                    Log.e(TAG, "❌ Bluetooth disabled")
                    stopSelf()
                    return@Thread
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.e(TAG, "❌ BLUETOOTH_CONNECT permission missing")
                    stopSelf()
                    return@Thread
                }

                Log.d(TAG, "🔍 Checking paired devices")
                adapter.bondedDevices.forEach {
                    Log.d(TAG, "Paired -> ${it.name} : ${it.address}")
                }

                val paired = adapter.bondedDevices.any {
                    it.address.equals(printerMac, ignoreCase = true)
                }

                if (!paired) {
                    Log.e(TAG, "❌ Printer NOT paired")
                    stopSelf()
                    return@Thread
                }

                Log.d(TAG, "✅ Printer is paired")

                connection = BluetoothConnection(printerMac)

                // ⚡ RETRY CONNECTION LOGIC
                var attempts = 0
                var connected = false
                while (!connected && attempts < 3) {
                    try {
                        Thread.sleep(500) // small delay before trying
                        connection.open()
                        connected = connection.isConnected
                    } catch (e: Exception) {
                        attempts++
                        Log.w(TAG, "Connection attempt $attempts failed", e)
                        Thread.sleep(300)
                    }
                }

                if (!connected) {
                    Log.e(TAG, "❌ Could not connect to printer after retries")
                    stopSelf()
                    return@Thread
                }

                Log.d(TAG, "🖨 Sending ZPL")
                connection.write(zpl.toByteArray())
                Log.d(TAG, "✅ Print success")

            } catch (e: Exception) {
                Log.e(TAG, "🔥 Print error", e)
            } finally {
                try {
                    connection?.close()
                    Log.d(TAG, "🔒 Connection closed")
                } catch (e: Exception) {
                    Log.e(TAG, "Close error", e)
                }

                stopSelf()
                Log.d(TAG, "🛑 Service stopped")
            }
        }.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Zebra Printer")
            .setContentText(text)
            .setSmallIcon(R.drawable.printer_white_on)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Zebra Printing",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }
}
