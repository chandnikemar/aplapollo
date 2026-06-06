package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.GateEntry.GateEntryResponse
import com.example.apolloapl.databinding.ItemGateEntryHistoryBinding

class GateEntryHistoryAdapter(
    private var list: MutableList<GateEntryResponse>,
    private val onItemClick: (GateEntryResponse) -> Unit
) : RecyclerView.Adapter<GateEntryHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGateEntryHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGateEntryHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val b = holder.binding

        // ---------------- MAIN DATA ----------------
        b.tvGateNo.text = item.gateEntryNo
        b.tvTransporter.text = item.transporterName
        b.tvVehicle.text = item.vehicleNumber
//        b.tvLrNo.text = "LR: ${item.lrNumber}"

        // Small subtitle (optional)
        b.tvDate.text = item.gateEntryType  // e.g. Inward / Outward

        // ---------------- CLICK ----------------
        b.root.setOnClickListener {
            onItemClick(item)
        }
    }

    // ================= UPDATE =================
    // ================= UPDATE =================
    fun updateList(newList: List<GateEntryResponse>) {

        list.clear()

        // ✅ Latest Added Item on Top
        list.addAll(
            newList.sortedByDescending {
                it.gateTransactionId
            }
        )

        notifyDataSetChanged()
    }

    // ================= OPTIONAL =================
    fun addItem(item: GateEntryResponse) {

        // ✅ Add at Top
        list.add(0, item)

        notifyItemInserted(0)
    }
    fun clearList() {
        list.clear()
        notifyDataSetChanged()
    }
}