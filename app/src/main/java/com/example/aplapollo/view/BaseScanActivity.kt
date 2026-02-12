package com.example.aplapollo.view



import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity

abstract class BaseScanActivity : AppCompatActivity() {

    private val scanBuffer = StringBuilder()
    private var lastKeyTime = 0L
    private val SCAN_TIMEOUT = 300L   // ms

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {

        if (event.action == KeyEvent.ACTION_DOWN) {

            val now = System.currentTimeMillis()

            // Reset if delay is too big (manual typing)
            if (now - lastKeyTime > SCAN_TIMEOUT) {
                scanBuffer.clear()
            }

            lastKeyTime = now

            val char = event.unicodeChar.toChar()

            if (char.code > 0) {
                scanBuffer.append(char)
            }

            // Scanner usually sends ENTER at end
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {

                val barcode = scanBuffer.toString().trim()
                scanBuffer.clear()

                if (barcode.isNotEmpty()) {
                    Log.d("BASE_SCANNER", "Scanned: $barcode")
                    onBarcodeScanned(barcode)
                }

                return true
            }
        }

        return super.dispatchKeyEvent(event)
    }

    /**
     * Child activity must implement this
     */
    abstract fun onBarcodeScanned(barcode: String)
}
