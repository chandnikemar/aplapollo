package com.example.aplapollo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.Slitting.OngoingJob
import com.example.apolloapl.R

class OngoingJobAdapter(
    private val jobList: List<OngoingJob>,
    private val onItemClick: (OngoingJob) -> Unit
) : RecyclerView.Adapter<OngoingJobAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBarcode: TextView = itemView.findViewById<TextView?>(R.id.tvBarcodeNo)
        val tvWidth: TextView = itemView.findViewById<TextView?>(R.id.tvWidth)
        val tvThickness: TextView = itemView.findViewById<TextView?>(R.id.tvThickness)
        val tvGrade: TextView = itemView.findViewById<TextView?>(R.id.tvGrade)

        val container: TableLayout =
            itemView.findViewById(R.id.commanBatchDetails)

        val divider: View? =
            itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
        val context = holder.itemView.context

        holder.tvBarcode.text = job.barcode
        holder.tvWidth.text = job.width.toString()
        holder.tvThickness.text = job.thickness
        holder.tvGrade.text = job.grade

        if (position == 0) {
            // ✅ Active job
            holder.container.setBackgroundColor(
                ContextCompat.getColor(context, R.color.job_highlight)
            )

            holder.itemView.isEnabled = true
            holder.itemView.isClickable = true

            holder.itemView.setOnClickListener {
                onItemClick(job)
            }

            holder.divider?.visibility = View.VISIBLE

        } else {
            // ❌ Inactive jobs
            holder.container.setBackgroundColor(
                ContextCompat.getColor(context, R.color.grey_light)
            )

            holder.itemView.isEnabled = false
            holder.itemView.isClickable = false

            holder.divider?.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = jobList.size
}
