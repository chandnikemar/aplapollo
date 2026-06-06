package com.example.aplapollo.adapter.Gp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.GP.BoMComponentResponse
import com.example.apolloapl.databinding.ItemCommonSearchBinding

class GpDialogAdapter (
    private val list: List<BoMComponentResponse>,
    private val onClick: (BoMComponentResponse) -> Unit
) : RecyclerView.Adapter<GpDialogAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCommonSearchBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemCommonSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.tvTitle.text =
            item.componentCode ?: ""

        holder.binding.tvSubTitle.text =
            item.materialDescription ?: ""

        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }
}