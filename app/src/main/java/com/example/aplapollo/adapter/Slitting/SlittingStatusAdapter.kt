package com.example.aplapollo.adapter.Slitting

import HrSlittingStatusTransactionDetail
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.ComponentAdapter
import com.example.aplapollo.adapter.OutputDialogAdapter
import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.model.Slitting.ComponentRequest
import com.example.apolloapl.R
import com.example.apolloapl.databinding.JoblistBinding

class SlittingStatusAdapter(
    private val context: Context,
    private val list: List<HrSlittingStatusTransactionDetail>,
    private val getBomOutputs: () -> List<BomOutput>,
    private val inputWeightTon: Double,
    private val onWeightChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<SlittingStatusAdapter.ViewHolder>() {

    private val weightMap = mutableMapOf<Int, Double>()
    private val componentMap = mutableMapOf<Int, MutableList<BomComponent>>()
    private val adapterMap = mutableMapOf<Int, ComponentAdapter>()
    private val selectedOutputMap = mutableMapOf<Int, BomOutput>()

    private val uomMap = mutableMapOf<Int, String>()
    private val units = listOf("Select", "Kg", "Tons")

    private var expandedPosition = -1

    inner class ViewHolder(val binding: JoblistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var watcher: TextWatcher? = null
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
        val rowId = item.hrSlittingTranDtlId

        // ---------------- Barcode ----------------
        b.tvBarcode.text = item.barcode ?: "No Barcode"

        // ---------------- Weight ----------------
        holder.watcher?.let { b.editC4.removeTextChangedListener(it) }

        val value = weightMap[rowId] ?: item.weighAfterSlitting ?: 0.0
        b.editC4.setText(if (value == 0.0) "" else value.toString())

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val weight = s.toString().toDoubleOrNull() ?: 0.0
                weightMap[rowId] = weight
                onWeightChanged?.invoke()
                validateComponentLimit(rowId) // ✅ validate on weight change
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        b.editC4.addTextChangedListener(watcher)
        holder.watcher = watcher

        // ---------------- UOM Spinner ----------------
        val adapter = object : ArrayAdapter<String>(
            context,
            android.R.layout.simple_spinner_item,
            units
        ) {
            override fun isEnabled(position: Int) = position != 0

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                view.setTypeface(null, if (position == 0) Typeface.NORMAL else Typeface.BOLD)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerUnit.adapter = adapter
        if (!uomMap.containsKey(rowId)) {
            uomMap[rowId] = "Tons"
        }
        val selectedUom = uomMap[rowId] ?: "Tons"
        val index = units.indexOf(selectedUom)
        b.spinnerUnit.setSelection(if (index >= 0) index else 0)

        b.spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {

                if (pos == 0) return

                val selectedUnit = units[pos]
                uomMap[rowId] = selectedUnit
                b.editC4.hint = "Weight ($selectedUnit)"

                (parent?.getChildAt(0) as? TextView)?.apply {
                    setTextColor(Color.BLACK)
                    setTypeface(null, Typeface.BOLD)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // ---------------- Output Material ----------------
        val selectedOutput = selectedOutputMap[rowId]
        b.tvOutputMaterial.text = selectedOutput?.outputMaterial ?: "Select Output Material"
        b.tvOutputDesc.text = selectedOutput?.materialDescription

        val isExpanded = position == expandedPosition
        b.layoutExpand.visibility = if (isExpanded) View.VISIBLE else View.GONE

        b.tvOutputMaterial.setOnClickListener {

            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_output_material, null)

            val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerOutput)

            val dialog = android.app.AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            val outputs = getBomOutputs()

            recycler.layoutManager = LinearLayoutManager(context)

            recycler.adapter = OutputDialogAdapter(outputs) { selected ->

                selectedOutputMap[rowId] = selected

                val newList = selected.boMComponent
                    ?.map { it.copy(  materialDescription = it.materialDescription ?: "",
                                       Uom =  "Kg") }
                    ?.toMutableList() ?: mutableListOf()

                componentMap[rowId] = newList

                adapterMap[rowId] = ComponentAdapter(
                    list = newList,
                    inputWeightTon = inputWeightTon
                ) {
                    validateComponentLimit(rowId)
                }

                notifyItemChanged(position)
                dialog.dismiss()
            }

            dialog.show()
        }

        // ---------------- Expand ----------------
        b.layoutHeader.setOnClickListener {
            val old = expandedPosition
            expandedPosition = if (isExpanded) -1 else position

            if (old != -1) notifyItemChanged(old)
            notifyItemChanged(position)
        }

        // ---------------- Component Recycler ----------------
        val compList = componentMap[rowId] ?: mutableListOf()
        componentMap[rowId] = compList

        val compAdapter = adapterMap[rowId] ?: ComponentAdapter(
            list = compList,
            inputWeightTon = inputWeightTon
        ) {
            validateComponentLimit(rowId)
        }.also {
            adapterMap[rowId] = it
        }
        b.recyclerComponent.layoutManager = LinearLayoutManager(context)
        b.recyclerComponent.adapter = compAdapter
    }


    private fun validateComponentLimit(rowId: Int) {

        val outputWeight = weightMap[rowId] ?: 0.0
        val outputUom = uomMap[rowId] ?: "Kg"

        val components = componentMap[rowId] ?: return

        // ✅ Convert output to KG
        val outputWeightKg = when (outputUom.lowercase()) {

            "tons", "ton" -> outputWeight * 1000

            else -> outputWeight
        }

        // ✅ Components already in KG
        val totalComponentKg = components.sumOf { it.weight }

        // ✅ Validation
        if (totalComponentKg > outputWeightKg) {

            Toast.makeText(
                context,
                "Component weight cannot exceed Output weight",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ✅ 5% validation from Input material
        val maxAllowedKg = (inputWeightTon * 1000) * 0.05

        if (totalComponentKg > maxAllowedKg) {

            Toast.makeText(
                context,
                "Component weight cannot exceed ${
                    String.format("%.2f", maxAllowedKg)
                } Kg (5% of Input Material)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    // ---------------- API Helpers ----------------

    fun getComponentsForRow(rowId: Int): List<ComponentRequest> {
        return componentMap[rowId]
            ?.filter { it.weight > 0 && it.componentCode.isNotBlank() }
            ?.map {
                ComponentRequest(
                    MaterialCode = it.componentCode.trim(),
                    MaterilDesc= it.materialDescription.toString(),
                    Weight = it.weight,
                    Uom = "Kg",
                )
            } ?: emptyList()
    }

    fun getSelectedOutputMaterial(rowId: Int): String {
        return selectedOutputMap[rowId]?.outputMaterial ?: "Ton"
    }

    fun getUpdatedTransactionDetails(): List<HrSlittingStatusTransactionDetail> {
        return list.map {
            val id = it.hrSlittingTranDtlId
            it.weighAfterSlitting = weightMap[id] ?: 0.0
            it
        }
    }
    fun getTotalOutputWeight(): Double {
        return list.sumOf { it.weighAfterSlitting ?: 0.0 }
    }
    fun getSelectedUom(rowId: Int): String {
        return uomMap[rowId] ?: "Kg"
    }
}