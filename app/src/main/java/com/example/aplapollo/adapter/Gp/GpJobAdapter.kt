package com.example.aplapollo.adapter.Gp

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput
import com.example.aplapollo.model.GP.GalvanizingTransactionDetailsResponse
import com.example.aplapollo.model.GP.GalvanizingTransactionResponse
import com.example.apolloapl.databinding.DynamicJobItemBinding

class GpJobAdapter(

    private val context: Context,

    private val jobList: MutableList<GalvanizingTransactionResponse>,

    private val onDelete: (
        position: Int,
        item: GalvanizingTransactionResponse
    ) -> Unit,

    private val onOutputClick: (Int) -> Unit,

    private var inputMaterialcode: String

) : RecyclerView.Adapter<GpJobAdapter.VH>() {

    inner class VH(
        val binding: DynamicJobItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        var watcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VH {

        val binding =
            DynamicJobItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return VH(binding)
    }

    override fun getItemCount(): Int =
        jobList.size

    override fun onBindViewHolder(
        holder: VH,
        position: Int
    ) {

        val b = holder.binding

        val item = jobList[position]

        val unitList = listOf("Tons")

        val spinnerAdapter =
            ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                unitList
            )

        spinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        b.jobLayout.spinnerUnit.adapter =
            spinnerAdapter

        val detail =
            getOrCreateDetail(item)



        b.jobLayout.tvBarcode.text =

            if (detail.barcode.isNullOrEmpty()) {

                item.motherBarcode ?: "-"

            } else {

                detail.barcode
            }



        b.jobLayout.tvOutputMaterial.text =

            detail.selectedOutputMaterial
                ?.outputMaterial
                ?: "Select Output Material"

        b.jobLayout.tvOutputDesc.text =

            detail.selectedOutputMaterial
                ?.materialDescription
                ?: "Select Output Material"

        // =====================================
        // REMOVE OLD WATCHER
        // =====================================

        holder.watcher?.let {

            b.jobLayout.editC4
                .removeTextChangedListener(it)
        }

        // =====================================
        // SET WEIGHT
        // =====================================

        val weightValue =
            detail.weightAfterGalvanizing ?: 0.0

        b.jobLayout.editC4.setText(

            if (weightValue == 0.0) {

                ""

            } else {

                weightValue.toString()
            }
        )

        // =====================================
        // CHECK ZINC COMPONENT
        // =====================================

        val hasZincComponent =

            detail.components?.any {

                it.isZincComponent == true

            } == true

        // =====================================
        // DISABLE WEIGHT FIELD
        // =====================================
//
//        if (hasZincComponent) {
//
//            b.jobLayout.editC4.apply {
//
//                    }
//
//        } else {
//
//            b.jobLayout.editC4.apply {
//
//                isEnabled = true
//
//                isFocusable = true
//
//                isFocusableInTouchMode = true
//
//                isClickable = true
//
//                isLongClickable = true
//
//                isCursorVisible = true
//
//                alpha = 1f
//            }
//        }

        // =====================================
        // TEXT WATCHER
        // =====================================

        val watcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                val enteredWeight =
                    s.toString().trim().toDoubleOrNull() ?: 0.0

                detail.weightAfterGalvanizing = enteredWeight

                android.util.Log.d(
                    "GP_WEIGHT",
                    "Position=$position Weight=$enteredWeight"
                )
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

        holder.watcher = watcher


        if (detail.components.isNullOrEmpty()) {

            b.jobLayout.layoutExpand.visibility =
                View.GONE

            b.jobLayout.recyclerComponent.visibility =
                View.GONE

        } else {

            b.jobLayout.layoutExpand.visibility =
                View.VISIBLE

            b.jobLayout.recyclerComponent.visibility =
                View.VISIBLE
        }

        if (detail.components == null) {

            detail.components =
                mutableListOf()
        }

        // =====================================
        // COMPONENT ADAPTER
        // =====================================

        val componentAdapter =
            ComponentGpAdapter(

                list =
                detail.components ?: mutableListOf(),

                inputWeightTon =
                detail.weightAfterGalvanizing ?: 0.0,

                zincMaterialCode =
                item.zincMaterialCode ?: "",

                zincWeight =
                item.zincWeight ?: 0.0
            )

        b.jobLayout.recyclerComponent.layoutManager =
            LinearLayoutManager(context)

        b.jobLayout.recyclerComponent.adapter =
            componentAdapter

        // =====================================
        // OUTPUT CLICK
        // =====================================

        b.jobLayout.layoutOutputMaterial
            .setOnClickListener {

                onOutputClick(position)
            }

        // =====================================
        // DELETE BUTTON
        // =====================================

        b.btnDeleteJob.visibility =

            if (position == 0) {

                View.GONE

            } else {

                View.VISIBLE
            }

        b.btnDeleteJob.setOnClickListener {

            val adapterPosition =
                holder.adapterPosition

            if (
                adapterPosition != RecyclerView.NO_POSITION &&
                adapterPosition < jobList.size
            ) {

                onDelete(
                    adapterPosition,
                    jobList[adapterPosition]
                )
            }
        }
    }

    // =====================================
    // SAFE DETAIL
    // =====================================

    private fun getOrCreateDetail(
        item: GalvanizingTransactionResponse
    ): GalvanizingTransactionDetailsResponse {

        if (item.galvanizingTransactionDetails.isNullOrEmpty()) {

            item.galvanizingTransactionDetails =

                mutableListOf(

                    GalvanizingTransactionDetailsResponse(

                        barcode = "",

                        weightAfterGalvanizing = 0.0,

                        components = mutableListOf()
                    )
                )
        }

        return item.galvanizingTransactionDetails!!
            .first()
    }


    fun setSelectedOutput(
        position: Int,
        output: BomOutput
    ) {

        if (position >= jobList.size) return

        val item =
            jobList[position]

        val detail =
            getOrCreateDetail(item)

        detail.selectedOutputMaterial =
            output

        val componentList =
            mutableListOf<BomComponent>()

        output.boMComponent?.forEach { component ->

            val isZincMatched =

                component.componentCode
                    ?.trim()
                    ?.equals(
                        item.zincMaterialCode
                            ?.trim(),
                        ignoreCase = true
                    ) == true

            val autoWeight =

                if (isZincMatched) {

                    item.zincWeight ?: 0.0

                } else {

                    0.0
                }

            componentList.add(

                BomComponent(

                    boMComponentId =
                    component.boMComponentId ?: 0,

                    boMOutputId =
                    component.boMOutputId ?: 0,

                    componentCode =
                    component.componentCode,

                    materialDescription =
                    component.materialDescription,

                    weight =
                    autoWeight,

                    Uom = "Kg",

                    isDisabled =
                    isZincMatched,

                    isZincComponent =
                    isZincMatched
                )
            )
        }

        detail.components =
            componentList

        notifyItemChanged(position)
    }



    fun addJob() {

        jobList.add(

            GalvanizingTransactionResponse(
                galvanizingTranId = 0
            )
        )

        notifyItemInserted(
            jobList.lastIndex
        )
    }



    fun getUpdatedList():
            MutableList<GalvanizingTransactionResponse> =
        jobList
}