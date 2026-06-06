package com.example.aplapollo.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.BomComponent
import com.example.apolloapl.databinding.ItemComponentBinding



class ComponentAdapter(

    private val list: MutableList<BomComponent>,
    private val inputWeightTon: Double,
    private val onComponentChanged: (() -> Unit)? = null

) : RecyclerView.Adapter<ComponentAdapter.ViewHolder>() {

    private val units = listOf("Kg")

    inner class ViewHolder(val binding: ItemComponentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var watcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemComponentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]
        val b = holder.binding

        // ================= BASIC DATA =================
        b.tvComponentCode.text = item.componentCode
        b.tvComponentDescription.text = item.materialDescription

        item.Uom = "Kg"

        // ================= SPINNER =================
        val adapter = object : ArrayAdapter<String>(
            holder.itemView.context,
            android.R.layout.simple_spinner_item,
            units
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                view.setTypeface(null, Typeface.BOLD)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerComponentUom.adapter = adapter
        b.spinnerComponentUom.setSelection(0)
        b.spinnerComponentUom.isEnabled = false
        b.spinnerComponentUom.isClickable = false

        // ================= WEIGHT INPUT =================
        holder.watcher?.let {
            b.etComponentWeight.removeTextChangedListener(it)
        }

        b.etComponentWeight.setText(
            if (item.weight > 0) item.weight.toString() else ""
        )

        val watcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

                val weight = s.toString().toDoubleOrNull() ?: 0.0


                item.weight = weight

                onComponentChanged?.invoke()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        b.etComponentWeight.addTextChangedListener(watcher)
        holder.watcher = watcher
    }



    fun getUpdatedList(): List<BomComponent> = list

    fun getUom(position: Int): String = "Kg"

    fun updateList(newList: List<BomComponent>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}