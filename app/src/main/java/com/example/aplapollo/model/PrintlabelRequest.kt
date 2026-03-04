package com.example.aplapollo.model

data class PrintLabelBarcodeRequest (
        val barcode: String,
        val locationId: Int,
        val createdBy: String,
        val createdDate: String
    )