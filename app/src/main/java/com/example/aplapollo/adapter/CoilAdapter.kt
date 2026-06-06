package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.apolloapl.R

class CoilAdapter(
    private val coilList: MutableList<String>,
    private val savedCoilList: MutableList<String>
) : RecyclerView.Adapter<CoilAdapter.CoilViewHolder>() {

    inner class CoilViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvCoilNo: TextView =
            itemView.findViewById(R.id.tvCoilNo)

        val btnDelete: ImageView =
            itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CoilViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coil, parent, false)

        return CoilViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CoilViewHolder,
        position: Int
    ) {

        val coil = coilList?.get(position)

        holder.tvCoilNo.text = coil


        if (savedCoilList.contains(coil)) {

            holder.btnDelete.visibility = View.GONE

            // Disable click
            holder.btnDelete.isEnabled = false

        } else {

            // New coil → allow delete
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.isEnabled = true
        }

        holder.btnDelete.setOnClickListener {


            if (savedCoilList.contains(coil)) {
                return@setOnClickListener
            }

            val adapterPosition = holder.adapterPosition

            if (adapterPosition != RecyclerView.NO_POSITION) {

                coilList?.removeAt(adapterPosition)

                notifyItemRemoved(adapterPosition)

                coilList?.let { it1 ->
                    notifyItemRangeChanged(
                        adapterPosition,
                        it1.size
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return coilList?.size ?: 0
    }
}