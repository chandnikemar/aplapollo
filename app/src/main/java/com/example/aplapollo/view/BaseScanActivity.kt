package com.example.aplapollo.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity

abstract class BaseScanActivity : AppCompatActivity() {

    private val scanBuffer = StringBuilder()

    private var lastScanTime = 0L

    private val scanHandler =
        Handler(Looper.getMainLooper())

    private val scanRunnable = Runnable {

        if (scanBuffer.isNotEmpty()) {

            val barcode =
                scanBuffer.toString().trim()

            Log.d(
                "BASE_SCANNER",
                "Scanned Barcode = $barcode"
            )

            onBarcodeScanned(barcode)

            scanBuffer.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.action == KeyEvent.ACTION_DOWN) {

            val currentTime =
                System.currentTimeMillis()

            // Reset if manual typing delay
            if (currentTime - lastScanTime > 1000) {
                scanBuffer.clear()
            }

            lastScanTime = currentTime

            when (event.keyCode) {

                KeyEvent.KEYCODE_ENTER -> {

                    scanHandler.removeCallbacks(scanRunnable)

                    val barcode =
                        scanBuffer.toString().trim()

                    if (barcode.isNotEmpty()) {

                        Log.d(
                            "BASE_SCANNER",
                            "ENTER Barcode = $barcode"
                        )

                        onBarcodeScanned(barcode)
                    }

                    scanBuffer.clear()

                    return true
                }

                else -> {

                    val pressedKey =
                        event.unicodeChar

                    if (pressedKey != 0) {

                        scanBuffer.append(
                            pressedKey.toChar()
                        )

                        // Auto detect scan completion
                        scanHandler.removeCallbacks(scanRunnable)

                        scanHandler.postDelayed(
                            scanRunnable,
                            200
                        )
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event)
    }

    abstract fun onBarcodeScanned(barcode: String)
}