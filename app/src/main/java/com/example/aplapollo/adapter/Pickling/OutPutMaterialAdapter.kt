//package com.example.aplapollo.adapter
//
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aplapollo.model.BomComponent
//import com.example.aplapollo.model.BomOutput
//import com.example.apolloapl.databinding.DynamicJobItemBinding
//
//class OutputMaterialAdapter(
//    private val list: MutableList<OutputMaterialUI>
//) : RecyclerView.Adapter<OutputMaterialAdapter.VH>() {
//
//    inner class VH(val binding: DynamicJobItemBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
//        val binding = DynamicJobItemBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        return VH(binding)
//    }
//
//    override fun getItemCount(): Int = list.size
//
//    override fun onBindViewHolder(holder: VH, position: Int) {
//
//        val item = list[position]
//        val b = holder.binding
//
//        bindUI(b, item, holder)
//    }
//
//    // =================================================
//    // BIND UI
//    // =================================================
//    private fun bindUI(
//        b: DynamicJobItemBinding,
//        item: OutputMaterialUI,
//        holder: VH
//    ) {
//
//        setOutput(b, item)
//        setWeight(b, item, holder)
//        setExpand(b, item)
//        setComponents(b, item)
//        setClicks(b, holder.adapterPosition)
//    }
//
//    // =================================================
//    // OUTPUT
//    // =================================================
//    private fun setOutput(b: DynamicJobItemBinding, item: OutputMaterialUI) {
//
//        b.jobLayout.tvOutputMaterial.text =
//            if (item.barcode.isBlank())
//                "Select Output Material"
//            else item.barcode
//    }
//
//    // =================================================
//    // WEIGHT (SAFE TEXTWATCHER)
//    // =================================================
//    private fun setWeight(
//        b: DynamicJobItemBinding,
//        item: OutputMaterialUI,
//        holder: VH
//    ) {
//
//        b.jobLayout.editC4.setOnFocusChangeListener(null)
//
//        b.jobLayout.editC4.setText(
//            if (item.weightAfterPickling == 0.0) ""
//            else item.weightAfterPickling.toString()
//        )
//
//        b.jobLayout.editC4.addTextChangedListener(object : TextWatcher {
//
//            override fun afterTextChanged(s: Editable?) {
//
//                val weight = s.toString().toDoubleOrNull() ?: 0.0
//
//                val pos = holder.adapterPosition
//                if (pos != RecyclerView.NO_POSITION) {
//                    list[pos].weightAfterPickling = weight
//                }
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//    }
//
//    // =================================================
//    // EXPAND
//    // =================================================
//    private fun setExpand(b: DynamicJobItemBinding, item: OutputMaterialUI) {
//        b.jobLayout.layoutExpand.visibility =
//            if (item.isExpanded) View.VISIBLE else View.GONE
//    }
//
//    // =================================================
//    // COMPONENTS (NO RECREATION BUG FIXED)
//    // =================================================
//    private fun setComponents(
//        b: DynamicJobItemBinding,
//        item: OutputMaterialUI
//    ) {
//
//        val recycler = b.jobLayout.recyclerComponent
//
//        if (recycler.adapter == null) {
//
//            val adapter = ComponentAdapter(
//                item.components,
//                inputWeightTon = item.weightAfterPickling
//            )
//
//            recycler.layoutManager =
//                LinearLayoutManager(b.root.context)
//
//            recycler.adapter = adapter
//
//        } else {
//
//            (recycler.adapter as ComponentAdapter)
//                .updateList(item.components)
//        }
//    }
//
//    // =================================================
//    // CLICKS
//    // =================================================
//    private fun setClicks(
//        b: DynamicJobItemBinding,
//        position: Int
//    ) {
//
//        b.jobLayout.layoutOutputMaterial.setOnClickListener {
//            selectOutputMaterial(position)
//        }
//
//        b.jobLayout.ivExpand.setOnClickListener {
//            toggleExpand(position)
//        }
//
//        b.btnDeleteJob.setOnClickListener {
//            removeOutput(position)
//        }
//    }
//
//    // =================================================
//    // ACTIONS
//    // =================================================
//    fun addOutput() {
//        list.add(OutputMaterialUI())
//        notifyItemInserted(list.lastIndex)
//    }
//
//    fun removeOutput(position: Int) {
//        if (position in list.indices) {
//            list.removeAt(position)
//            notifyItemRemoved(position)
//        }
//    }
//
//    fun updateWeight(position: Int, weight: Double) {
//        if (position in list.indices) {
//            list[position].weightAfterPickling = weight
//            notifyItemChanged(position)
//        }
//    }
//
//    fun selectOutputMaterial(position: Int) {
//        if (position in list.indices) {
//            list[position].barcode = "SELECTED"
//            notifyItemChanged(position)
//        }
//    }
//
//    fun toggleExpand(position: Int) {
//        if (position in list.indices) {
//            list[position].isExpanded = !list[position].isExpanded
//            notifyItemChanged(position)
//        }
//    }
//
//    fun updateComponents(
//        position: Int,
//        components: MutableList<BomComponent>
//    ) {
//        if (position in list.indices) {
//            list[position].components = components
//            notifyItemChanged(position)
//        }
//    }
//
//    fun submitList(newList: List<OutputMaterialUI>) {
//        list.clear()
//        list.addAll(newList)
//        notifyDataSetChanged()
//    }
//
//    fun getList(): List<OutputMaterialUI> = list
//
//
//}
//data class OutputMaterialUI(
//    var barcode: String = "",
//    var weightAfterPickling: Double = 0.0,
//    var components: MutableList<BomComponent> = mutableListOf(),
//    var isExpanded: Boolean = false,
//    var output: BomOutput? = null
//)