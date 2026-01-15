package com.example.aplapollo.adapter.Slitting



import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R

class SlittingWidthAdapter :
    RecyclerView.Adapter<SlittingWidthAdapter.WidthViewHolder>() {

    private val widthList = mutableListOf<Double>()

    fun submitList(list: List<Double>) {
        widthList.clear()
        widthList.addAll(list)
        notifyDataSetChanged()
    }

    inner class WidthViewHolder(val textView: TextView) :
        RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slitting_width, parent, false) as TextView
        return WidthViewHolder(view)
    }

    override fun onBindViewHolder(holder: WidthViewHolder, position: Int) {
        holder.textView.text = widthList[position].toString()
    }

    override fun getItemCount(): Int = widthList.size
}

