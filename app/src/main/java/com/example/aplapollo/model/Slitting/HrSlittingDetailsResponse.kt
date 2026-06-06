import com.example.aplapollo.model.BomComponent
import com.example.aplapollo.model.BomOutput

data class HrSlittingStatusResponse(
    val hrSlittingTranId: Int?,
    val locationId: Int?,
    val materialCode: String?,
    val motherCoilWeight: Double?,
    val locationName: String?,
    val sourceStockId: Int?,
    val motherBarcode: String?,
    val uoM: String?,
    val jobNumber: String?,
    val ironLossWeight: Double?,
    val scrapWeight: Double?,
    val completedBy: String?,
    val completedDate: String?,
    val status: String?,
    val remarks: String?,
    val totalRecord: Int?,
    val isPlanned: Boolean?,
    val allowedOutputWeightInTons: Double?,
    val allowedScrapWeightKg: Double?,
    val allowedToleranceWeightKg: Double?,
    val hRSlittingTransactionDetail: List<HrSlittingStatusTransactionDetail>?,
    val isActive: Boolean?,
    val createdBy: String?,
    val createdDate: String?,
    val modifiedBy: String?,
    val modifiedDate: String?,
    val tenantCode: String?,
    val tenantGroupCode: String?
)
data class HrSlittingStatusTransactionDetail(
    val hrSlittingTranDtlId: Int,
    val hrSlittingTranId: Int,
    val width: Double,
    val barcode: String,

    var weighAfterSlitting: Double? = 0.0,

    val weightTakenBy: String? = null,
    val weightLocationId: Int? = null,
    val weightDatetime: String? = null,

    val isActive: Boolean = true,
    val status: String = "",

    var selectedOutput: BomOutput? = null,
    var components: MutableList<BomComponent> = mutableListOf()
)