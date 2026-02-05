    package com.example.aplapollo.view.Slitting

    import android.app.ProgressDialog
    import android.os.Bundle
    import android.util.Log
    import android.view.View
    import android.widget.EditText
    import androidx.appcompat.app.AppCompatActivity
    import androidx.databinding.DataBindingUtil
    import androidx.lifecycle.ViewModelProvider
    import com.example.aplapollo.api.RetrofitInstance
    import com.example.aplapollo.helper.Constants
    import com.example.aplapollo.helper.LogoutHelper
    import com.example.aplapollo.helper.Resource
    import com.example.aplapollo.helper.SessionExpiredEvent
    import com.example.aplapollo.helper.SessionManager
    import com.example.aplapollo.model.Slitting.HRSlittingTransactionDetailRequest
    import com.example.aplapollo.model.Slitting.InitiateSlittingWithoutPlanRequest
    import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanViewModelfactory
    import com.example.aplapollo.viewmodel.slittingwithoutplan.SlittingWithoutplanvViewModel
    import com.example.apolloapl.R
    import com.example.apolloapl.databinding.ActivitySlittingplan3Binding
    import es.dmoral.toasty.Toasty

    class Slittingplan3Activity<ImageButton : View?> : AppCompatActivity() {
        private lateinit var binding: ActivitySlittingplan3Binding
        private lateinit var slittingWithoutplanvViewModel: SlittingWithoutplanvViewModel
        private lateinit var progress: ProgressDialog
        private lateinit var session: SessionManager
        private var baseUrl: String = ""
        private var userName: String? = ""
        private var token: String? = ""
//        private  var tenantCode:String?=""
        private  var userDetail: HashMap<String, Any?>?=null
        private var serverIpSharedPrefText: String? = null
        private var serverHttpPrefText: String? = null
        private var locationId:Int=0
        private var sourceStockId: Int = 0
        private var scannedBarcode: String? = null
        private  var tenantCode:String?=null
        private var transactionId:Int=0
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = DataBindingUtil.setContentView(this,R.layout.activity_slittingplan3)
            supportActionBar?.hide()
            progress = ProgressDialog(this)
            progress.setMessage("Please Wait...")
            val retrofitInstance =
                RetrofitInstance.getInstance(applicationContext)
            val viewModelProviderFactory = SlittingWithoutplanViewModelfactory(application, retrofitInstance)
            slittingWithoutplanvViewModel = ViewModelProvider(this, viewModelProviderFactory)[SlittingWithoutplanvViewModel::class.java]
            binding.idLayoutHeader.tvTitle.text = "WithOut Plan "
            session = SessionManager(this)
            userDetail = session.getUserDetails()
            binding.idLayoutHeader.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            if (userDetail!!.isEmpty()) {
                Toasty.error(this, "User details are missing.", Toasty.LENGTH_SHORT).show()
            } else {

                token = userDetail!!["jwtToken"].toString()
                userName = userDetail!!["userName"].toString()
//                tenantCode= userDetail!![SessionManager.Key_tenantCode].toString()
                serverIpSharedPrefText = userDetail!![Constants.KEY_SERVER_IP].toString()
                serverHttpPrefText = userDetail!![Constants.KEY_HTTP].toString()
                baseUrl = "$serverHttpPrefText://$serverIpSharedPrefText/"
                // ⭐ PRINT TOKEN HERE
                Log.d("JWT_TOKEN_QC", "JWT Token = $token")
                Log.d("Tanent_Code","Tenant Code= $tenantCode")
            }
            locationId=intent.getIntExtra("Location_ID",0)

            binding.layoutBatchDetails.visibility = View.GONE
            binding.layoutWeightContainer.removeAllViews()
            SessionExpiredEvent.logoutLiveData.observe(this) { shouldLogout ->
                if (shouldLogout == true) {
                    SessionExpiredEvent.logoutLiveData.value = false
                    LogoutHelper.handleLogout(this, session)
                }
            }
            binding.idLayoutHeader.ivBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            binding.commanInputRow.btnSearch.setOnClickListener {

                val barcode = binding.commanInputRow.inputField.text.toString().trim()

                if (barcode.isEmpty()) {
                    Toasty.warning(this, "Please scan barcode").show()
                    return@setOnClickListener
                }

                slittingWithoutplanvViewModel
                    .getStockByBatchOrBarcode( barcode)
            }

            binding.btnAddPlan.setOnClickListener {
                if (binding.layoutWeightContainer.childCount == 0) {
                    addWeightRow()
                }
            }


            slittingWithoutplanvViewModel.stockByBarcodeLiveData.observe(this) { resource ->

                when (resource) {

                    is Resource.Loading -> {
                        Log.d("SLITTING_PLAN_3", "Loading stock")
                        progress.show()
                    }

                    is Resource.Success -> {
                        progress.dismiss()

                        val stock = resource.data ?: return@observe

                        Log.d("SLITTING_PLAN_3", "Stock = $stock")

                        binding.layoutBatchDetails.visibility = View.VISIBLE

                        binding.inCommanBatch.tvItemCode.text =
                            "Item Code : ${stock.materialCode}"

                        binding.inCommanBatch.tvGrade.text =
                            "Grade : ${stock.grade}"

                        binding.inCommanBatch.tvWidth.text =
                            "Width : ${stock.width} MM"

                        binding.inCommanBatch.tvThickness.text =
                            "Thickness : ${stock.thickness} MM"

                        binding.inCommanBatch.tvWeight.text =
                            "Weight : ${stock.weight} KG"

                        sourceStockId = stock.stockId
                        scannedBarcode = stock.barcode
                        tenantCode=stock.tenantCode
                        transactionId=stock.transactionId?:0
                        Toasty.success(this, "Stock fetched successfully").show()
                    }

                    is Resource.Error -> {
                        progress.dismiss()
                        Log.e("SLITTING_PLAN_3", "Error = ${resource.message}")
                        Toasty.error(this, resource.message ?: "Error").show()
                    }
                }
            }

            slittingWithoutplanvViewModel.initiateSlittingWithoutPlanLiveData
                .observe(this) { resource ->

                    when (resource) {

                        is Resource.Loading -> {
                            progress.show()
                        }

                        is Resource.Success -> {
                            progress.dismiss()

                            Toasty.success(
                                this,
                                "Slitting initiated successfully"
                            ).show()
                            Log.d("SLITTING_WO_PLAN_API", """
                    API Success
                    Response = ${resource.data}
                """.trimIndent())

                            finish() // or navigate
                        }

                        is Resource.Error -> {
                            progress.dismiss()

                            Toasty.error(
                                this,
                                resource.message ?: "Failed to initiate slitting"
                            ).show()
                        }
                    }
                }


            binding.btncSaves.setOnClickListener {
                if (sourceStockId == 0) {
                    Toasty.warning(this, "Please scan coil first").show()
                    return@setOnClickListener
                }
                val weights = getAllWeights()

                if (weights.isEmpty() || weights.any { it.isBlank() }) {
                    Toasty.warning(this, "Please enter weight").show()
                    return@setOnClickListener
                }

                val request = InitiateSlittingWithoutPlanRequest(
                    HRSlittingTranId = 0,
                    TenantCode = tenantCode ?: "",
                    HRSlittingPlanId = 0,
                    LocationId = locationId,
                    SourceStockId = sourceStockId,
                    JobNumber = null,
                    Barcode = scannedBarcode,         // 🔥 scanned barcode
                    IronLossWeight = null,
                    ScrapWeight = null,
                    CompletedBy = "",
                    CompletedDate = "",
                    Status = "",
                    IsActive = true,
                    Remarks = "Slitting without plan",
                    IsPlanned=false,
                    hrSlittingTransactionDetail =
                    buildTransactionDetails(0)
                )

                slittingWithoutplanvViewModel
                    .initiateSlittingWithoutPlan( request)
            }
            binding.btncClears.setOnClickListener {

                binding.commanInputRow.inputField.text?.clear()
                binding.layoutBatchDetails.visibility = View.GONE
                binding.layoutWeightContainer.removeAllViews()

                sourceStockId = 0
                scannedBarcode = null
            }


       }
        private fun addWeightRow() {

            val rowView = layoutInflater.inflate(
                R.layout.item_weight_row,
                binding.layoutWeightContainer,
                false
            )

            val etWeight = rowView.findViewById<EditText>(R.id.etWeight)
            val btnEdit = rowView.findViewById<ImageButton>(R.id.btnEdit)
            val btnAddMore = rowView.findViewById<ImageButton>(R.id.btnAddMore)

            etWeight.isEnabled = true
            btnEdit?.visibility = View.GONE
            btnAddMore?.visibility = View.VISIBLE

            btnEdit?.setOnClickListener {
                etWeight.isEnabled = true
                etWeight.requestFocus()
                etWeight.setSelection(etWeight.text.length)
            }

            btnAddMore?.setOnClickListener {
                if (etWeight.text.isNullOrBlank()) {
                    Toasty.warning(this, "Enter weight first").show()
                    return@setOnClickListener
                }

                etWeight.isEnabled = false
                btnAddMore?.visibility = View.GONE
                btnEdit?.visibility = View.VISIBLE

                addWeightRow()
            }

            binding.layoutWeightContainer.addView(rowView)
        }

        private fun getAllWeights(): List<String> {
            val weights = mutableListOf<String>()
            for (i in 0 until binding.layoutWeightContainer.childCount) {
                val row = binding.layoutWeightContainer.getChildAt(i)
                val etWeight = row.findViewById<EditText>(R.id.etWeight)
                weights.add(etWeight.text.toString())
            }
            return weights
        }
        private fun buildTransactionDetails(
            hrSlittingTranId: Int
        ): List<HRSlittingTransactionDetailRequest> {

            val details = mutableListOf<HRSlittingTransactionDetailRequest>()

            val weights = getAllWeights()   // ← you already have this method

            weights.forEach { weightStr ->

                if (weightStr.isNotBlank()) {
                    details.add(
                        HRSlittingTransactionDetailRequest(
                            HRSlittingTranDtlId = 0,
                            HRSlittingTranId = 0,
                            Width = weightStr.toDouble(),
                            Barcode = null,
                            WeighAfterSlitting = null,
                            WeightTakenBy = null,
                            WeightLocationId = 0,
                            WeightDatetime = null,

                            IsActive = true,
                            Status = "InProgress"
                        )
                    )
                }
            }

            return details
        }

    }