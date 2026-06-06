package com.example.aplapollo.adapter.Slitting

import HrSlittingStatusTransactionDetail
import android.R
import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.ComponentAdapter
import com.example.aplapollo.helper.Utils
import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput
import com.example.apolloapl.databinding.DynamicJobItemBinding

class SlittingStatusAdapter(

    private val context: Context,
    private val list: MutableList<HrSlittingStatusTransactionDetail>,
    private val getBomOutputs: () -> List<BomOutput>,

    private val onAddClick: (Int, HrSlittingStatusTransactionDetail) -> Unit,
    private val onDeleteClick: (Int, HrSlittingStatusTransactionDetail) -> Unit

) : RecyclerView.Adapter<SlittingStatusAdapter.VH>() {

    inner class VH(val binding: DynamicJobItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var weightWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DynamicJobItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {

        val item = list[position]
        val b = holder.binding

        // =========================
        // UNIT SPINNER
        // =========================

        val unitList = listOf("Tons")

        val spinnerAdapter = ArrayAdapter(
            context,
            R.layout.simple_spinner_item,
            unitList
        )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        b.jobLayout.spinnerUnit.adapter = spinnerAdapter

        // =========================
        // BARCODE
        // =========================

        b.jobLayout.tvBarcode.text = item.barcode

        // =========================
        // WEIGHT
        // =========================

        holder.weightWatcher?.let {
            b.jobLayout.editC4.removeTextChangedListener(it)
        }

        b.jobLayout.editC4.setText(
            if ((item.weighAfterSlitting ?: 0.0) == 0.0)
                ""
            else
                item.weighAfterSlitting.toString()
        )

        val watcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                val currentPos = holder.adapterPosition

                if (currentPos != RecyclerView.NO_POSITION) {

                    list[currentPos].weighAfterSlitting =
                        s.toString().toDoubleOrNull() ?: 0.0
                }
            }

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
            }
        }

        b.jobLayout.editC4.addTextChangedListener(watcher)
        holder.weightWatcher = watcher

        // =========================
        // COMPONENT VISIBILITY
        // =========================

        if (item.components.isNullOrEmpty()) {

            b.jobLayout.layoutExpand.visibility = View.GONE
            b.jobLayout.recyclerComponent.visibility = View.GONE

        } else {

            b.jobLayout.layoutExpand.visibility = View.VISIBLE
            b.jobLayout.recyclerComponent.visibility = View.VISIBLE
        }

        // =========================
        // OUTPUT MATERIAL
        // =========================

        b.jobLayout.tvOutputMaterial.text =
            item.selectedOutput?.outputMaterial
                ?: "Select Output Material"

        b.jobLayout.tvOutputDesc.text =
            item.selectedOutput?.materialDescription
                ?: "Select Output Material"

        b.jobLayout.layoutOutputMaterial.setOnClickListener {

            val bomList = getBomOutputs()

            if (bomList.isEmpty()) {

                Utils.showErrorDialog(
                    context as Activity,
                    "BOM data not available yet"
                )

                return@setOnClickListener
            }

            Utils.showSearchableOutputDialog(
                activity = context as Activity,
                items = bomList
            ) { selected ->

                item.selectedOutput = selected

                item.components =
                    selected.boMComponent?.map {

                        BomComponent(
                            boMComponentId = 0,
                            boMOutputId = 0,
                            componentCode = it.componentCode,
                            materialDescription = it.materialDescription ?: "",
                            weight = 0.0,
                            Uom = "Kg"
                        )

                    }?.toMutableList() ?: mutableListOf()

                val pos = holder.adapterPosition

                if (pos != RecyclerView.NO_POSITION) {
                    notifyItemChanged(pos)
                }
            }
        }

        // =========================
        // COMPONENT RECYCLER
        // =========================

        val componentList = item.components ?: mutableListOf()

        b.jobLayout.recyclerComponent.layoutManager =
            LinearLayoutManager(context)

        b.jobLayout.recyclerComponent.adapter =
            ComponentAdapter(
                componentList,
                inputWeightTon = item.weighAfterSlitting ?: 0.0
            ) {}

        b.jobLayout.recyclerComponent.visibility =
            if (
                item.selectedOutput != null &&
                componentList.isNotEmpty()
            ) View.VISIBLE
            else View.GONE
        // =========================
        // DELETE BUTTON
        // =========================

        if (position == 0) {
            b.btnDeleteJob.visibility = View.GONE
        } else {
            b.btnDeleteJob.visibility = View.VISIBLE
        }

        b.btnDeleteJob.setOnClickListener {
            onDeleteClick(position, item)
        }
    }

    fun getUpdatedList(): MutableList<HrSlittingStatusTransactionDetail> {
        return list
    }

    fun getTotalOutputWeight(): Double {

        return list.sumOf {
            it.weighAfterSlitting ?: 0.0
        }
    }

    fun getSelectedOutputMaterial(rowId: Int): String {

        return list.find {
            it.hrSlittingTranDtlId == rowId
        }?.selectedOutput?.outputMaterial ?: ""
    }

    fun getTotalComponentWeight(): Double {

        return list.sumOf { item ->

            item.components.sumOf { comp ->
                comp.weight ?: 0.0
            }
        }
    }
}