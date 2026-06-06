package com.example.aplapollo.adapter.GateEntry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.GateEntry.GateTransactionItem
import com.example.apolloapl.R

class AddedCoilAdapter(
    private val activeCoils: List<GateTransactionItem>
) : RecyclerView.Adapter<AddedCoilAdapter.ViewHolder>() {

      inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvCoil: TextView =
            itemView.findViewById(R.id.tvCoil)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_added_coil, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        // ✅ Show only coilBatch
        holder.tvCoil.text =
            activeCoils[position].coilBatch
    }

    override fun getItemCount(): Int {
        return activeCoils.size
    }
}