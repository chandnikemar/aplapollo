//package com.example.aplapollo.adapter.Pickling
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.core.widget.addTextChangedListener
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aplapollo.model.Pickling.PicklingTransactionResponse
//import com.example.apolloapl.databinding.JoblistBinding
//
//class PicklingTransactionAdapter(
//    private val list: List<PicklingTransactionResponse>
//) : RecyclerView.Adapter<PicklingTransactionAdapter.ViewHolder>() {
//
//    private val weightMap = mutableMapOf<Int, String>()
//
//    inner class ViewHolder(val binding: JoblistBinding) :
//        RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//
//        val binding = JoblistBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        val item = list[position]
//        val b = holder.binding
//        val id = item.picklingTranId
//
//        // Barcode
//        b.textC2.text = item.barcode
//
//        // Enable edit
//        b.editC4.isEnabled = true
//
//        // Remove old listener (IMPORTANT)
//        b.editC4.clearFocus()
//        b.editC4.setOnFocusChangeListener(null)
//
//        // Set value
//        b.editC4.setText(
//            weightMap[id] ?: item.weightAfterPickling?.toString().orEmpty()
//        )
//
//        // Add listener safely
//        b.editC4.addTextChangedListener { editable ->
//
//            val text = editable?.toString() ?: ""
//
//            weightMap[id] = text
//        }
//    }
//
//    override fun getItemCount(): Int = list.size
//
//
//    // ✅ Get Updated Data For API
//    fun getUpdatedTransactionDetails(): List<PicklingTransactionResponse> {
//
//        return list.map { item ->
//
//            val id = item.picklingTranId
//
//            val updatedWeight =
//                weightMap[id]?.toDoubleOrNull() ?: 0.0
//
//            item.weightAfterPickling = updatedWeight
//
//            item
//        }
//    }
//}
