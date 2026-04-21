package com.example.aplapollo.view.ProductionEntry

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.adapter.FGAdapter
import com.example.aplapollo.adapter.FGItem
import com.example.apolloapl.R
import com.example.apolloapl.databinding.ActivityOutputProductionBinding

class OutputProductionActivity : AppCompatActivity() {
    private lateinit var binding :ActivityOutputProductionBinding
   private lateinit var adapter: FGAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding= DataBindingUtil.setContentView(this,R.layout.activity_output_production)
        adapter = FGAdapter()
        addNewCard()
//        val materialList = listOf("Steel", "Iron", "Copper", "Aluminum")
//        val tvMaterial: AutoCompleteTextView = itemView.findViewById(R.id.tvMaterial)
//        val adapter = ArrayAdapter(
//            this,
//            android.R.layout.simple_dropdown_item_1line,
//            materialList
//        )
//        binding.topMaterialDropdown.setAdapter(adapter)


    }
    private fun addNewCard() {

        val view = layoutInflater.inflate(
            R.layout.layout_output_card,
            binding.containerLayout,
            false
        )

        // 🔹 Access inner views if needed
        val rvFG = view.findViewById<RecyclerView>(R.id.rvFGList)

        rvFG.layoutManager = LinearLayoutManager(this)
        val fgAdapter = FGAdapter()
        rvFG.adapter = fgAdapter

        // Dummy FG data
        fgAdapter.setData(
            listOf(
                FGItem("MAT1", "100"),
                FGItem("MAT2", "120")
            )
        )

        binding.containerLayout.addView(view)
    }
}