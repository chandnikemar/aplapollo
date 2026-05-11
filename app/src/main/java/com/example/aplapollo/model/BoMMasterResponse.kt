package  com.example.aplapollo.model
data class BomResponse(
        val boMId: Int,
        val boMNumber: String,
        val inputMaterial: String,
        val boMOutput: List<BomOutput>,
        val isActive: Boolean,
        val createdBy: String,
        val createdDate: String,
        val modifiedBy: String,
        val modifiedDate: String,
        val tenantCode: String,
        val tenantGroupCode: String?
    )

data class BomOutput(
    val boMOutputId: Int? = null,
    val boMMasterId: Int? = null,
    val outputMaterial: String? = null,
    val materialDescription: String? = null,
    val boMComponent: List<BomComponent>? = emptyList()
)

    data class BomComponent(
        val boMComponentId: Int,
        val boMOutputId: Int,
        val componentCode: String,
       val  materialDescription :String?,
        var weight:Double=0.0,
        var Uom: String?
    )