    package com.example.aplapollo.adapter.Slitting

    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.example.aplapollo.model.Slitting.HrSlittingTransactionDetail
    import com.example.apolloapl.databinding.JoblistBinding
    import es.dmoral.toasty.Toasty

    class SlittingStatusAdapter(
        private val list: List<HrSlittingTransactionDetail>,
        private val onWeightChanged: () -> Unit   // callback to activity
    ) : RecyclerView.Adapter<SlittingStatusAdapter.ViewHolder>() {

        private val weightMap = mutableMapOf<Int, Double>()

        inner class ViewHolder(val binding: JoblistBinding) :
            RecyclerView.ViewHolder(binding.root) {

            var textWatcher: android.text.TextWatcher? = null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding = JoblistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val item = list[position]
            val b = holder.binding
            val id = item.hrSlittingTranDtlId


            // Remove old watcher (IMPORTANT)
            holder.textWatcher?.let {
                b.editC4.removeTextChangedListener(it)
            }


            b.textC2.text = item.barcode


            // Set value safely
            val value = weightMap[id] ?: item.weighAfterSlitting ?: 0.0

            if (b.editC4.text.toString() != value.toString()) {
                b.editC4.setText(value.toString())
            }


            // Create new watcher
            val watcher = object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {}

                override fun afterTextChanged(s: android.text.Editable?) {

                    val text = s.toString()

                    val weight = text.toDoubleOrNull() ?: 0.0

                    if (weight <= 0 && text.isNotEmpty()) {

                        Toasty.warning(
                            b.editC4.context,
                            "Weight must be > 0"
                        ).show()

                        return
                    }

                    weightMap[id] = weight


                    // Notify activity → recalc iron loss
                    onWeightChanged()
                }
            }


            b.editC4.addTextChangedListener(watcher)

            holder.textWatcher = watcher
        }


        override fun getItemCount(): Int = list.size


        fun getUpdatedTransactionDetails(): List<HrSlittingTransactionDetail> {

            return list.map { item ->

                val id = item.hrSlittingTranDtlId

                item.weighAfterSlitting = weightMap[id] ?: 0.0

                item
            }
        }
    }
