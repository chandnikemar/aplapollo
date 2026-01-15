package com.example.aplapollo.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.Utils
import com.example.apolloapl.databinding.ActivityPrinterMacaddBinding

class PrinterMACAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrinterMacaddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrinterMacaddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load previously saved MAC address if any
        val savedMac = Utils.getSharedPrefs(this, Constants.KEY_PRINTER_MAC)
        binding.etSelectPrinter.setText(savedMac)

        // Make input automatically uppercase
        binding.etSelectPrinter.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isEditing && s != null) {
                    isEditing = true
                    val upper = s.toString().uppercase()
                    if (upper != s.toString()) {
                        s.replace(0, s.length, upper)
                    }
                    isEditing = false
                }
            }
        })

        // Save button click
        binding.btnUpdateSelectPrinter.setOnClickListener {
            var mac = binding.etSelectPrinter.text.toString().trim().uppercase()

            // Remove any non-hex characters first
            mac = mac.replace("[^A-F0-9]".toRegex(), "")

            if (mac.length == 12) {
                // Insert ':' every 2 characters
                val formattedMac = mac.chunked(2).joinToString(":")

                // Save the formatted MAC
                Utils.setSharedPrefs(this, Constants.KEY_PRINTER_MAC, formattedMac)

                Log.d("PrinterMACAddActivity", "Saved MAC Address: $formattedMac")

                finish()
            } else {
                binding.etSelectPrinter.error = "Enter valid 12-digit MAC address"
            }
        }

    }

}
