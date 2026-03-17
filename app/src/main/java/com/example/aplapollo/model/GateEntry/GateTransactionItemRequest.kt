package com.example.aplapollo.model.GateEntry

data class CoilSubmitRequest(
    val gateTransactionItemId: Int,
    val gateTransactionId: Int,
    val gateEntryNo: String,
    val gateTransactionItems: List<Coils> = listOf()
)
data class Coils(
    val coilBatch: String
)