package com.example.aplapollo.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.BoMComponentResponse
import com.example.aplapollo.model.Slitting.ComponentRequest
import com.example.apolloapl.R

class ComponentAdapter(
    private val list: List<BoMComponentResponse>
) : RecyclerView.Adapter<ComponentAdapter.ViewHolder>() {

    // Store entered weights
    private val weightMap = mutableMapOf<Int, Double>()

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {

        val tvComponent = binding.findViewById<TextView>(R.id.tvComponentCode)
        val etWeight = binding.findViewById<EditText>(R.id.etWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bom_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = list[position]

        holder.tvComponent.text = item.componentCode

        // Restore value if already entered
        holder.etWeight.setText(weightMap[position]?.toString() ?: "")

        holder.etWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val weight = s.toString().toDoubleOrNull() ?: 0.0
                weightMap[position] = weight
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun getItemCount() = list.size

    // ✅ Get updated data with weights
    fun getUpdatedComponents(): List<ComponentRequest> {
        return list.mapIndexed { index, item ->
            ComponentRequest(
                MaterialCode = item.componentCode,
                Weight = weightMap[index] ?: 0.0
            )
        }
    }
}