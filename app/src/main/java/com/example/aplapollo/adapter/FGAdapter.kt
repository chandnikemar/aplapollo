package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R
import com.google.android.material.textfield.TextInputEditText

class FGAdapter : RecyclerView.Adapter<FGAdapter.FGViewHolder>() {

    private val list = ArrayList<FGItem>()

    fun setData(data: List<FGItem>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    inner class FGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFgLabel: TextView = itemView.findViewById(R.id.tvFgLabel)
        val tvMaterial: AutoCompleteTextView = itemView.findViewById(R.id.tvMaterial)
        val tvWeight: TextInputEditText = itemView.findViewById(R.id.tvWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FGViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fg, parent, false)
        return FGViewHolder(view)
    }

    override fun onBindViewHolder(holder: FGViewHolder, position: Int) {
        val item = list[position]

        holder.tvFgLabel.text = "FG ${position + 1}"
        holder.tvMaterial.setText(item.material)
        holder.tvWeight.setText(item.weight)
    }

    override fun getItemCount(): Int = list.size
}
data class FGItem(
    val material: String,
    val weight: String
)