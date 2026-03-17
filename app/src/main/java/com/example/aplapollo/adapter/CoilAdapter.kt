package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R

class CoilAdapter(
    private val coilList: MutableList<String>
) : RecyclerView.Adapter<CoilAdapter.CoilViewHolder>() {

    inner class CoilViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCoilNo: TextView = itemView.findViewById(R.id.tvCoilNo)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoilViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coil, parent, false)
        return CoilViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoilViewHolder, position: Int) {

        val coil = coilList[position]
        holder.tvCoilNo.text = coil

        // Delete coil
        holder.btnDelete.setOnClickListener {
            coilList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, coilList.size)
        }
    }

    override fun getItemCount(): Int = coilList.size
}