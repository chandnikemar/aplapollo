package com.example.aplapollo.adapter.Slitting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R

class OutputQtyAdapter(
    private val items: List<OutputItem>
) : RecyclerView.Adapter<OutputQtyAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvQty: TextView = view.findViewById(R.id.tvOutQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_output_qty, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.tvQty.text = items[position].qty.toString()
    }

    override fun getItemCount() = items.size
}


data class OutputItem(
    val code: String,
    val qty: Int
)
