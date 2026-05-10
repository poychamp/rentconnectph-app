package ph.rentconnect.app.feature.detail.data

import kotlinx.serialization.Serializable

@Serializable
data class InquiryRequest(
    val name: String,
    val phone: String,
)

@Serializable
data class InquiryValidationErrorResponse(
    val message: String,
    val errors: Map<String, List<String>> = emptyMap(),
)
