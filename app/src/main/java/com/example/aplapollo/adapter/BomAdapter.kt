package com.example.aplapollo.adapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.*
import com.example.apolloapl.R

class BomAdapter(
    private val inputList: List<BoMMasterResponse>
) : RecyclerView.Adapter<BomAdapter.InputVH>() {

    inner class InputVH(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bom_parent, parent, false)
        return InputVH(view)
    }

    override fun getItemCount(): Int = inputList.size

    override fun onBindViewHolder(holder: InputVH, position: Int) {

        val input = inputList[position]

        val tvInput = holder.view.findViewById<TextView>(R.id.tvInput)
        val etInputWeight = holder.view.findViewById<EditText>(R.id.etInputWeight)
        val outputContainer = holder.view.findViewById<LinearLayout>(R.id.outputContainer)

        tvInput.text = input.inputMaterial
        etInputWeight.setText(input.inputWeight.toString())

        // ✅ Capture INPUT weight
        etInputWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                input.inputWeight = s.toString().toDoubleOrNull() ?: 0.0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ✅ Expand / Collapse OUTPUT
        tvInput.setOnClickListener {

            if (outputContainer.childCount > 0) {
                outputContainer.removeAllViews()
                return@setOnClickListener
            }

            loadOutputs(outputContainer, input.boMOutput)
        }
    }

    // ================= OUTPUT =================

    private fun loadOutputs(
        container: LinearLayout,
        outputs: List<BoMOutputResponse>
    ) {

        outputs.forEach { output ->

            val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_bom_output, container, false)

            val tvOutput = view.findViewById<TextView>(R.id.tvOutput)
            val etOutputWeight = view.findViewById<EditText>(R.id.etOutputWeight)
            val componentContainer =
                view.findViewById<LinearLayout>(R.id.componentContainer)

            tvOutput.text = output.outputMaterial
            etOutputWeight.setText(output.weight.toString())

            // ✅ Capture OUTPUT weight
            etOutputWeight.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    output.weight = s.toString().toDoubleOrNull() ?: 0.0
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // ✅ Expand / Collapse COMPONENT
            tvOutput.setOnClickListener {

                if (componentContainer.childCount > 0) {
                    componentContainer.removeAllViews()
                    return@setOnClickListener
                }

                loadComponents(componentContainer, output.boMComponent)
            }

            container.addView(view)
        }
    }

    // ================= COMPONENT =================

    @SuppressLint("MissingInflatedId")
    private fun loadComponents(
        container: LinearLayout,
        components: List<BoMComponentResponse>
    ) {

        components.forEach { comp ->

            val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_bom_component, container, false)

            val tv = view.findViewById<TextView>(R.id.tvComponent)
            val et = view.findViewById<EditText>(R.id.etWeight)

            tv.text = comp.componentCode
            et.setText(comp.weight.toString())

            // ✅ COMPONENT WEIGHT
            et.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    comp.weight = s.toString().toDoubleOrNull() ?: 0.0
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            container.addView(view)
        }
    }
    // ================= GET FINAL DATA =================

    fun getUpdatedData(): List<BoMMasterResponse> {
        return inputList
    }
}