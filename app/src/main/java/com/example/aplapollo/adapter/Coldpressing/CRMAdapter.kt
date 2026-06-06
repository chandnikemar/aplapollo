    package com.example.aplapollo.adapter.Coldpressing

    import android.R
    import android.annotation.SuppressLint
    import android.content.Context
    import android.os.Build
    import android.text.Editable
    import android.text.TextWatcher
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import androidx.annotation.RequiresApi
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.example.aplapollo.adapter.ComponentAdapter
    import com.example.aplapollo.helper.Utils
    import com.example.aplapollo.model.BomComponent
    import com.example.aplapollo.model.BomOutput
    import com.example.aplapollo.model.CRM.CRMComponentRequest
    import com.example.aplapollo.model.CRM.CRMTransactionDetailRequest
    import com.example.aplapollo.model.CRM.CRMTransactionDetailResponse
    import com.example.aplapollo.model.CRM.CRMTransactionResponse

    import com.example.apolloapl.databinding.DynamicJobItemBinding

    class CRMAdapter(

        private val context: Context,

        private val jobList:
        MutableList<CRMTransactionResponse>,

        private val onDelete: (
            position: Int,
            item: CRMTransactionResponse
        ) -> Unit,

        private val onOutputClick: (Int) -> Unit

    ) : RecyclerView.Adapter<CRMAdapter.VH>() {

        inner class VH(
            val binding: DynamicJobItemBinding
        ) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): VH {

            val binding =
                DynamicJobItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

            return VH(binding)
        }

        override fun getItemCount(): Int =
            jobList.size

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(
            holder: VH,
            position: Int
        ) {

            val item = jobList[position]
            val b = holder.binding

            val unitList = listOf("Tons")

            val spinnerAdapter = ArrayAdapter(
                context,
                R.layout.simple_spinner_item,
                unitList
            )

            spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            b.jobLayout.spinnerUnit.adapter = spinnerAdapter

            // ======================================
            // SAFE DETAIL
            // ======================================

            val detail =
                getOrCreateDetail(item)



            val barcodeText = when {

                !detail.barcode.isNullOrEmpty() -> {
                    detail.barcode!!
                }

                !item.barcode.isNullOrEmpty() -> {
                    item.barcode!!
                }

                !item.motherBarcode.isNullOrEmpty() -> {
                    item.motherBarcode!!
                }

                else -> "-"
            }

            Log.e("CRM_BARCODE", barcodeText)

            b.jobLayout.tvBarcode.text = barcodeText
            // ======================================
            // OUTPUT MATERIAL
            // ======================================
            b.jobLayout.tvOutputMaterial.text =

                detail.selectedOutputMaterial
                    ?.outputMaterial
                    ?: "Select Output Material"

            // ======================================
            // DESCRIPTION
            // ======================================

            b.jobLayout.tvOutputDesc.text =
                detail.selectedOutputMaterial
                    ?.materialDescription
                    ?: "Select Output Material"

            // ======================================
            // WEIGHT
            // ======================================

            val oldWatcher =
                b.jobLayout.editC4.tag as? TextWatcher

            if (oldWatcher != null) {
                b.jobLayout.editC4.removeTextChangedListener(oldWatcher)
            }

            b.jobLayout.editC4.setText(
                if ((detail.weightAfterCrm ?: 0.0) == 0.0) ""
                else detail.weightAfterCrm.toString()
            )

            val watcher = object : TextWatcher {

                override fun afterTextChanged(s: Editable?) {

                    detail.weightAfterCrm =
                        s.toString().toDoubleOrNull() ?: 0.0
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}
            }

            b.jobLayout.editC4.addTextChangedListener(watcher)
            b.jobLayout.editC4.tag = watcher

            // ======================================
            // COMPONENT VISIBILITY
            // ======================================

            if (detail.components.isNullOrEmpty()) {

                b.jobLayout.layoutExpand.visibility = View.GONE
                b.jobLayout.recyclerComponent.visibility = View.GONE

            } else {

                b.jobLayout.layoutExpand.visibility = View.VISIBLE
                b.jobLayout.recyclerComponent.visibility = View.VISIBLE
            }

            // ======================================
            // COMPONENT RECYCLER
            // ======================================

            if (detail.components == null) {
                detail.components = mutableListOf()
            }

            val componentAdapter =
                ComponentAdapter(
                    detail.components
                        ?: mutableListOf(),
                    inputWeightTon = detail.weightAfterCrm ?: 0.0
                )

            b.jobLayout.recyclerComponent.layoutManager =
                LinearLayoutManager(context)

            b.jobLayout.recyclerComponent.adapter =
                componentAdapter

            // ======================================
            // OUTPUT CLICK
            // ======================================

            b.jobLayout.layoutOutputMaterial.setOnClickListener {
                onOutputClick(position)
            }
            if (position == 0) {

                b.btnDeleteJob.visibility = View.GONE

            } else {

                b.btnDeleteJob.visibility = View.VISIBLE
            }
            b.btnDeleteJob.setOnClickListener {

                val adapterPosition = holder.adapterPosition

                if (
                    adapterPosition != RecyclerView.NO_POSITION &&
                    adapterPosition < jobList.size
                ) {

                    val deletedItem =
                        jobList[adapterPosition]

                    onDelete(
                        adapterPosition,
                        deletedItem
                    )
                }
            }

            // ======================================
            // DELETE
            // ======================================

    //        b.btnDeleteJob.setOnClickListener {
    //
    //            if (position < jobList.size) {
    //
    //                jobList.removeAt(position)
    //
    //                notifyItemRemoved(position)
    //
    //                onDelete(position)
    //            }
    //        }
        }

        // =========================================
        // SAFE DETAIL
        // =========================================

        private fun getOrCreateDetail(
            item: CRMTransactionResponse
        ): CRMTransactionDetailResponse {

            if (item.crmTransactionDetails.isNullOrEmpty()) {

                item.crmTransactionDetails =
                    mutableListOf(

                        CRMTransactionDetailResponse(
                            barcode = "",
                            weightAfterCrm = 0.0,
                            components = mutableListOf()
                        )
                    )
            }

            return item.crmTransactionDetails!!.first()
        }

        // =========================================
        // SET OUTPUT
        // =========================================

        @SuppressLint("NotifyDataSetChanged")
        fun setSelectedOutput(
            position: Int,
            output: BomOutput
        ) {

            if (position >= jobList.size) return

            val item = jobList[position]

            val detail = getOrCreateDetail(item)

            // =========================
            // SET OUTPUT
            // =========================

            detail.selectedOutputMaterial = output

            Log.e("OUTPUT_DEBUG", output.toString())

            // =========================
            // FETCH BARCODE FROM API RESPONSE
            // =========================

            val apiBarcode =
                item.crmTransactionDetails
                    ?.firstOrNull()
                    ?.barcode

            detail.barcode =
                apiBarcode
                    ?: item.barcode
                            ?: item.motherBarcode
                            ?: ""

            Log.e("OUTPUT_BARCODE", detail.barcode ?: "")

            // =========================
            // COMPONENTS
            // =========================

            val componentList =
                mutableListOf<BomComponent>()

            output.boMComponent?.forEach {

                componentList.add(

                    BomComponent(

                        boMComponentId = 0,

                        boMOutputId = 0,

                        componentCode = it.componentCode,

                        materialDescription =
                        it.materialDescription,

                        weight = 0.0,

                        Uom = "Kg"
                    )
                )
            }

            detail.components = componentList

            notifyItemChanged(position)
        }
        // =========================================
        // ADD JOB
        // =========================================
// =========================================
// GET ALL COMPONENTS
// =========================================
        fun getTotalOutputWeight(): Double {

            return jobList.sumOf { job ->

                job.crmTransactionDetails
                    ?.firstOrNull()
                    ?.weightAfterCrm ?: 0.0
            }
        }
        fun getAllComponents():
                List<CRMComponentRequest> {

            val componentList =
                mutableListOf<CRMComponentRequest>()

            jobList.forEach { job ->

                val detail =
                    job.crmTransactionDetails
                        ?.firstOrNull()

                detail?.components
                    ?.filter {

                        it.weight!! > 0
                    }
                    ?.forEach { component ->

                        componentList.add(

                            CRMComponentRequest(

                                MaterialCode =
                                component.componentCode
                                    ?: "",

                                Weight =
                                component.weight,

                                Uom =
                                component.Uom
                                    ?: "Kg"
                            )
                        )
                    }
            }

            return componentList
        }
        fun addJob() {

            jobList.add(
                CRMTransactionResponse(
                    crmTranId = 0,

                )
            )

            notifyItemInserted(jobList.lastIndex)
        }

        // =========================================
        // UPDATE LIST
        // =========================================

        fun updateList(list: List<CRMTransactionResponse>) {

            jobList.clear()
            jobList.addAll(list)
            notifyDataSetChanged()
        }

        // =========================================
        // GET LIST
        // =========================================
        @RequiresApi(Build.VERSION_CODES.O)
        fun getCRMTransactionDetails(
            userName: String,
            output: String
        ): MutableList<CRMTransactionDetailRequest> {

            val details =
                mutableListOf<CRMTransactionDetailRequest>()

            jobList.forEach { job ->

                val detail =
                    job.crmTransactionDetails
                        ?.firstOrNull()

                if (detail != null) {

                    val componentList =
                        mutableListOf<CRMComponentRequest>()

                    // =========================
                    // COMPONENTS
                    // =========================

                    detail.components
                        ?.forEach { component ->

                            componentList.add(

                                CRMComponentRequest(

                                    MaterialCode =
                                    component.componentCode ?: "",

                                    Weight =
                                    component.weight ?: 0.0,

                                    Uom =
                                    component.Uom ?: "KG"
                                )
                            )
                        }

                    // =========================
                    // BARCODE FROM API RESPONSE
                    // =========================

                    val finalBarcode = when {

                        !detail.barcode.isNullOrEmpty() -> {
                            detail.barcode!!
                        }

                        !job.barcode.isNullOrEmpty() -> {
                            job.barcode!!
                        }

                        !job.motherBarcode.isNullOrEmpty() -> {
                            job.motherBarcode!!
                        }

                        else -> {
                            output
                        }
                    }

                    details.add(

                        CRMTransactionDetailRequest(

                            crmTransactionDetailsId =
                            detail.crmTransactionDetailsId ?: 0,

                            barcode = finalBarcode,

                            materialCode =
                            detail.selectedOutputMaterial
                                ?.outputMaterial ?: "",

                            weightAfterCRM =
                            detail.weightAfterCrm ?: 0.0,

                            uoM = "Tons",

                            weightTakenBy =
                            userName,

                            weightDateTime =
                            Utils.getCurrentDateTimeISO(),

                            crmComponent =
                            componentList
                        )
                    )
                }
            }

            return details
        }
        fun getTotalComponentWeight(): Double {

            return jobList.sumOf { job ->

                job.crmTransactionDetails
                    ?.firstOrNull()
                    ?.components
                    ?.sumOf { component ->

                        component.weight ?: 0.0
                    } ?: 0.0
            }
        }
        fun getUpdatedList(): MutableList<CRMTransactionResponse> {
            return jobList
        }
    }