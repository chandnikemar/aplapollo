package com.example.aplapollo.adapter.Coldpressing

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.ComponentAdapter
import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.model.CRM.ComponentsRequest
import com.example.apolloapl.databinding.JoblistBinding

class CRMAdapter(
    private val list: MutableList<CrmItem>,
    private var bomOutputs: List<BomOutput>,
    private val inputWeightTon: Double,
    private val onWeightChanged: () -> Unit
) : RecyclerView.Adapter<CRMAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: JoblistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var watcher: TextWatcher? = null
        var componentAdapter: ComponentAdapter? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = JoblistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val b = holder.binding

        // ---------------- Barcode ----------------
        b.tvBarcode.text = item.barcode

        // ---------------- Weight ----------------
        holder.watcher?.let { b.editC4.removeTextChangedListener(it) }

        val value = if (item.weight > 0) item.weight.toString() else ""
        b.editC4.setText(value)
        b.editC4.setSelection(value.length)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                item.weight = s.toString().toDoubleOrNull() ?: 0.0
                onWeightChanged()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        b.editC4.addTextChangedListener(watcher)
        holder.watcher = watcher

        // ---------------- Output Material ----------------
        b.tvOutputMaterial.text =
            item.selectedOutput?.outputMaterial ?: "Select Output Material"

        // ---------------- Expand ----------------
        b.layoutExpand.visibility =
            if (item.isExpanded && item.componentList.isNotEmpty())
                View.VISIBLE
            else
                View.GONE
        // ---------------- Component Recycler ----------------
        if (holder.componentAdapter == null) {

            holder.componentAdapter = ComponentAdapter(
                list = item.componentList,
                inputWeightTon = inputWeightTon
            ) {
                onWeightChanged()
            }

            b.recyclerComponent.layoutManager =
                LinearLayoutManager(holder.itemView.context)

            b.recyclerComponent.adapter = holder.componentAdapter

        } else {
            holder.componentAdapter?.updateList(item.componentList)
        }

        // ---------------- OUTPUT CLICK ----------------
        b.tvOutputMaterial.setOnClickListener {

            if (bomOutputs.isEmpty()) {
                Toast.makeText(holder.itemView.context, "No BOM found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val names = bomOutputs.map { it.outputMaterial }

            AlertDialog.Builder(holder.itemView.context)
                .setItems(names.toTypedArray()) { _, index ->

                    val selected = bomOutputs[index]

                    item.selectedOutput = selected

                    val newList = selected.boMComponent?.map { it.copy() }

                    item.componentList.clear()
                    if (newList != null) {
                        item.componentList.addAll(newList)
                    }

                    // ✅ VERY IMPORTANT
                    item.isExpanded = true

                    // ✅ FORCE UI UPDATE
                    notifyItemChanged(position)
                }
                .show()
        }

        // ---------------- ROW CLICK ----------------
        b.layoutHeader.setOnClickListener {
            item.isExpanded = !item.isExpanded
            notifyItemChanged(position)
        }
    }

    // ================= HELPERS =================

    fun getAllItems(): List<CrmItem> = list

    fun getTotalWeight(): Double =
        list.sumOf { it.weight }

    fun getSelectedOutputMaterial(): String =
        list.firstOrNull()?.selectedOutput?.outputMaterial ?: ""

    fun getComponents(): List<ComponentsRequest> {
        return list.flatMap { item ->
            item.componentList
                .filter {
                    !it.componentCode.isNullOrBlank() && (it.weight ?: 0.0) > 0
                }
                .map {
                    ComponentsRequest(
                        MaterialCode = it.componentCode!!,
                        Weight = it.weight ?: 0.0
                    )
                }
        }
    }

    // 🔥 IMPORTANT: Create row using barcode
    fun updateList(barcodes: List<String>) {
        list.clear()
        list.addAll(barcodes.map { CrmItem(barcode = it) })
        notifyDataSetChanged()
    }

    // 🔥 Apply BOM + auto show components
    fun setBomOutputs(newOutputs: List<BomOutput>) {
        bomOutputs = newOutputs

        if (list.isNotEmpty() && bomOutputs.isNotEmpty()) {

            val first = bomOutputs.first()

            list.forEach { item ->
                item.selectedOutput = first

                item.componentList.clear()
                first.boMComponent?.let {
                    item.componentList.addAll(
                        it.map { it.copy() }
                    )
                }

                item.isExpanded = true
            }

            notifyDataSetChanged()
        }
    }
}

// ================= DATA MODEL =================

data class CrmItem(
    val barcode: String,
    var weight: Double = 0.0,
    var selectedOutput: BomOutput? = null,
    var componentList: MutableList<BomComponent> = mutableListOf(),
    var isExpanded: Boolean = false
)