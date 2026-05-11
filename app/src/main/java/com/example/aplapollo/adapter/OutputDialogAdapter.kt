package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.BomOutput
import com.example.apolloapl.R

class OutputDialogAdapter(
    private val list: List<BomOutput>,
    private val onClick: (BomOutput) -> Unit
) : RecyclerView.Adapter<OutputDialogAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvMaterial: TextView = view.findViewById(R.id.tvMaterial)
        val tvDesc: TextView = view.findViewById(R.id.tvDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_output_dialog, parent, false)
        return VH(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.tvMaterial.text = item.outputMaterial
        holder.tvDesc.text = item.materialDescription ?: ""

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }
}