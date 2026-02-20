package com.example.aplapollo.adapter.Coldpressing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aplapollo.model.CRM.OngoingCRMJobResponse
import com.example.apolloapl.R

class OngoingCRMJobAdapter(
    private var jobList: List<OngoingCRMJobResponse>,
    private val onItemClick: (OngoingCRMJobResponse) -> Unit
) : RecyclerView.Adapter<OngoingCRMJobAdapter.JobViewHolder>() {

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBarcode: TextView = itemView.findViewById(R.id.tvBarcodeNo)
        val tvWidth: TextView = itemView.findViewById(R.id.tvWidth)
        val tvThickness: TextView = itemView.findViewById(R.id.tvThickness)
        val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
        val container: TableLayout = itemView.findViewById(R.id.commanBatchDetails)
        val divider: View? = itemView.findViewById(R.id.divider)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobList[position]
//        val stock = job.stockTransaction
        val context = holder.itemView.context

        holder.tvBarcode.text = job?.barcode ?: "-"
        holder.tvWidth.text = job?.width?.toString() ?: "-"
        holder.tvThickness.text = job?.thickness?.toString() ?: "-"
        holder.tvGrade.text = job?.grade ?: "-"

        if (position == 0) {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(context, R.color.job_highlight)
            )
            holder.itemView.isEnabled = true
            holder.itemView.isClickable = true
            holder.itemView.setOnClickListener { onItemClick(job) }
            holder.divider?.visibility = View.VISIBLE
        } else {
            holder.container.setBackgroundColor(
                ContextCompat.getColor(context, R.color.grey_light)
            )
            holder.itemView.isEnabled = false
            holder.itemView.isClickable = false
            holder.divider?.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = jobList.size
    fun updateList(newList: List<OngoingCRMJobResponse>) {
        jobList = newList
        notifyDataSetChanged()
    }

}
