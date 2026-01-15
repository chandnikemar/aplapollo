package com.example.aplapollo.adapter.Slitting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.Slitting.HrSlittingTransactionDetail
import com.example.apolloapl.databinding.JoblistBinding

class SlittingStatusAdapter(
    private val list: List<HrSlittingTransactionDetail>
) : RecyclerView.Adapter<SlittingStatusAdapter.ViewHolder>() {

    private val weightMap = mutableMapOf<Int, String>()

    inner class ViewHolder(val binding: JoblistBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = JoblistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val b = holder.binding
        val id = item.hrSlittingTranDtlId

        b.textC2.text = item.barcode

        // Always editable
        b.editC4.isEnabled = true
        b.editC4.setText(
            weightMap[id] ?: item.weighAfterSlitting.toString()
        )

        // Update weight on text change
        b.editC4.addTextChangedListener {
            val text = it.toString()
            weightMap[id] = if (text.isBlank()) " " else text
        }
    }

    override fun getItemCount(): Int = list.size

    // Get updated transaction details for API
    fun getUpdatedTransactionDetails(): List<HrSlittingTransactionDetail> {
        return list.map { item ->
            val id = item.hrSlittingTranDtlId
            val updatedWeight = weightMap[id]?.toDoubleOrNull() ?: 00.0
            item.weighAfterSlitting = updatedWeight
            item
        }
    }
}
