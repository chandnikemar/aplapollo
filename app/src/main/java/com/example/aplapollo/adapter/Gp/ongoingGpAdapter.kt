    package com.example.aplapollo.adapter.Gp

    import android.annotation.SuppressLint
    import android.app.AlertDialog
    import android.content.Context
    import android.content.res.ColorStateList
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.core.content.ContextCompat
    import androidx.recyclerview.widget.RecyclerView
    import com.example.aplapollo.helper.Utils
    import com.example.aplapollo.model.GP.GpOngoingJobsResponse
    import com.example.apolloapl.R
    import com.google.android.material.button.MaterialButton

    class OngoingGpAdapter(

        private val jobList: MutableList<GpOngoingJobsResponse>,
        private val onDeleteClick: (GpOngoingJobsResponse) -> Unit,
        private val onItemClick: (GpOngoingJobsResponse) -> Unit,
        private val onReprintClick: (GpOngoingJobsResponse) -> Unit

    ) : RecyclerView.Adapter<OngoingGpAdapter.JobViewHolder>() {

        inner class JobViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {

            val tvBarcode: TextView = itemView.findViewById(R.id.tvBarcodeNo)
            val tvDate: TextView = itemView.findViewById(R.id.tvDate)
            val tvWidth: TextView = itemView.findViewById(R.id.tvWidth)
            val tvThickness: TextView = itemView.findViewById(R.id.tvThickness)
            val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
            val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

            val btnAction: MaterialButton = itemView.findViewById(R.id.btnAction)

            val ivEye: MaterialButton? = itemView.findViewById(R.id.ivEye)
            val ivDelete: MaterialButton? = itemView.findViewById(R.id.btnDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ongoing_job, parent, false)

            return JobViewHolder(view)
        }

        override fun getItemCount(): Int = jobList.size

        override fun onBindViewHolder(holder: JobViewHolder, position: Int) {

            val job = jobList[position]
            val context = holder.itemView.context

            val child = job.galvanizingJobDetailsResponse?.firstOrNull()
            val status = job.status ?: ""
            val isCompleted = status.equals("Completed", true)


            holder.tvBarcode.text = job?.barcode ?: "-"
            holder.tvDate.text = Utils.formatDate(job.createdDateTime)
            holder.tvWidth.text = if (job.width > 0) job.width.toString() else "-"
            holder.tvThickness.text = if (job.thickness > 0) job.thickness.toString() else "-"
            holder.tvGrade.text = job.grade ?: "-"
            holder.tvStatus.text = status


            holder.ivEye?.visibility = if (isCompleted) View.VISIBLE else View.GONE
    //            holder.ivDelete?.visibility = if (isCompleted) View.GONE else View.VISIBLE

            holder.itemView.alpha = if (isCompleted) 0.6f else 1.0f

            // disable click for completed
            holder.itemView.isClickable = !isCompleted
            holder.itemView.isFocusable = !isCompleted


            when (status.lowercase()) {

                "inprogress" -> {
                    holder.btnAction.visibility = View.VISIBLE
                    holder.btnAction.text = "Delete"
                    holder.btnAction.setIconResource(android.R.drawable.ic_menu_delete)

                    holder.btnAction.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(context, android.R.color.holo_red_dark)
                        )
                }

                "completed" -> {
                    holder.btnAction.visibility = View.VISIBLE
                    holder.btnAction.text = "Print"
                    holder.btnAction.setIconResource(android.R.drawable.ic_menu_revert)

                    holder.btnAction.backgroundTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                        )
                }

                else -> holder.btnAction.visibility = View.GONE
            }

            // =========================
            // CLICK EVENTS
            // =========================

            holder.itemView.setOnClickListener {
                if (!isCompleted) {
                    onItemClick(job)
                }
            }

            holder.btnAction.setOnClickListener {
                when (status.lowercase()) {

                    "inprogress" -> onDeleteClick(job)

                    "completed" -> onReprintClick(job)
                }
            }

            holder.ivEye?.setOnClickListener {
                if (isCompleted) {
                    showCompletedDialog(context,job)
                }
            }

            holder.ivDelete?.setOnClickListener {
                if (!isCompleted) {
                    onDeleteClick(job)
                }
            }
        }


        @SuppressLint("ResourceType")
        private fun showCompletedDialog(
            context: Context,
            job: GpOngoingJobsResponse
        ) {

            val dialog = LayoutInflater.from(context)
                .inflate(R.layout.dialog_completed_job, null)

            val tvBarcode = dialog.findViewById<TextView>(R.id.tvBarcode)
            val tvWeight = dialog.findViewById<TextView>(R.id.tvWeight)
            val btnOk = dialog.findViewById<MaterialButton>(R.id.btnOk)

            val child = job.galvanizingJobDetailsResponse?: emptyList()
            if (child.isEmpty()) {

                tvBarcode.text = "-"
                tvWeight.text = "-"

            } else {

                tvBarcode.text = child.joinToString("\n") {
                    it.barcode ?: "-"
                }

                tvWeight.text = child.joinToString("\n") {
                    "${it.weight ?: 0.0} Ton"
                }
            }

            val alert = AlertDialog.Builder(context)
                .setView(dialog)
                .create()

            btnOk.setOnClickListener {
                alert.dismiss()
            }

            alert.show()
        }

        fun removeItem(tranId: Int) {

            val position = jobList.indexOfFirst {
                it.galvanizingTranId == tranId
            }

            Log.d(
                "REMOVE_ITEM",
                "tranId=$tranId position=$position"
            )

            if (position != -1) {
                jobList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, jobList.size)
            }
        }
        @SuppressLint("NotifyDataSetChanged")
        fun updateList(newList: List<GpOngoingJobsResponse>) {
            jobList.clear()
            jobList.addAll(newList)
            notifyDataSetChanged()
        }
    }
