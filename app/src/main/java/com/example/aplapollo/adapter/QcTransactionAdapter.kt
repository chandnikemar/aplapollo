package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.QualityCheck.QcTransactionResponse
import com.example.apolloapl.databinding.ItemQcHistoryBinding

class QcHistoryAdapter(
    private val onReprintClick: (QcTransactionResponse) -> Unit
) : RecyclerView.Adapter<QcHistoryAdapter.QcViewHolder>() {

    private val qcList = ArrayList<QcTransactionResponse>()

    inner class QcViewHolder(
        private val binding: ItemQcHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QcTransactionResponse) {

            binding.apply {

                tvBarcode.text = item.barcode ?: "-"

                tvSupplierName.text = item.supplierName ?: "-"

                tvSupplierBatchNo.text = item.supplierBatchNo ?: "-"

                tvGrade.text = item.grade ?: "-"

                tvThickness.text =
                    String.format("%.2f", item.thickness ?: 0.0)

                tvGrnDate.text = item.grnDate
                    ?.substringBefore("T")
                    ?: "-"

                tvQcStatus.text = item.status ?: "-"

                when (item.status?.uppercase()) {

                    "ACCEPTED" -> {
                        tvQcStatus.text = "QC APPROVED"
                        tvQcStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                android.R.color.holo_green_dark
                            )
                        )
                    }

                    "REJECTED" -> {
                        tvQcStatus.text = "QC REJECTED"
                        tvQcStatus.setTextColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                android.R.color.holo_red_dark
                            )
                        )
                    }

                    else -> {
                        tvQcStatus.text = item.status ?: "-"
                    }
                }

                btnReprint.setOnClickListener {
                    onReprintClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QcViewHolder {

        val binding = ItemQcHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return QcViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: QcViewHolder,
        position: Int
    ) {
        holder.bind(qcList[position])
    }

    override fun getItemCount(): Int = qcList.size

    fun submitList(list: List<QcTransactionResponse>) {
        qcList.clear()
        qcList.addAll(list)
        notifyDataSetChanged()
    }
}