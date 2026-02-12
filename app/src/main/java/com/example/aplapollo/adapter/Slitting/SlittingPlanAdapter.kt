package com.example.aplapollo.adapter.Slitting



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R

class SlittingWidthAdapter(

) : RecyclerView.Adapter<SlittingWidthAdapter.WidthViewHolder>() {

    private val widthList = mutableListOf<Double>()

    fun submitList(list: List<Double>) {
        widthList.clear()
        widthList.addAll(list)
        notifyDataSetChanged()
    }

    inner class WidthViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val etWeight: TextView = itemView.findViewById(R.id.etWeight)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidthViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_width_row, parent, false)

        return WidthViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidthViewHolder, position: Int) {

        holder.etWeight.text = String.format("%.2f", widthList[position])

    }

    override fun getItemCount(): Int = widthList.size
}
