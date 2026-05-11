package com.example.aplapollo.adapter.GateEntry

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.GateEntry.TransporterResponse

class TransporterDialogAdapter(
    private val onClick: (TransporterResponse) -> Unit
) : RecyclerView.Adapter<TransporterDialogAdapter.VH>() {

    private var list: List<TransporterResponse> = emptyList()
    private var fullList: List<TransporterResponse> = emptyList()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = list[position]

        holder.text.text = "${item.transporterCode} - ${item.transporterName}"

        holder.itemView.setOnClickListener {
            onClick(item)   // ✅ NOW WORKS
        }
    }

    // ================= UPDATE LIST =================
    fun updateList(newList: List<TransporterResponse>) {
        fullList = newList
        list = newList
        notifyDataSetChanged()
    }

    // ================= SEARCH FILTER =================
    fun filter(query: String) {

        list = if (query.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.transporterCode.contains(query, true) ||
                        it.transporterName.contains(query, true)
            }
        }

        notifyDataSetChanged()
    }
}