package ph.rentconnect.app.feature.detail.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InquiryRequest(
    val name: String,
    val phone: String,
)

@Serializable
data class InquirySuccessResponse(
    val success: Boolean,
    val listing: InquiryListingDto,
)

@Serializable
data class InquiryListingDto(
    val title: String,
    val barangay: String,
    @SerialName("contact_type_label") val contactTypeLabel: String? = null,
    @SerialName("listing_contact") val listingContact: InquiryListingContactDto? = null,
)

@Serializable
data class InquiryListingContactDto(
    val phone: String? = null,
    val name: String? = null,
    val notes: String? = null,
)

@Serializable
data class InquiryValidationErrorResponse(
    val message: String,
    val errors: Map<String, List<String>> = emptyMap(),
)
