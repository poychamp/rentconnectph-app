package ph.rentconnect.app.feature.detail.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListingDetailResponse(
    val listing: ListingDetail,
)

@Serializable
data class ListingDetail(
    val id: Int,
    val uuid: String,
    val title: String,
    val type: String,
    @SerialName("type_label") val typeLabel: String,
    @SerialName("price_monthly") val priceMonthly: Int,
    val beds: Int? = null,
    val baths: Int? = null,
    val sqm: Int? = null,
    val barangay: String,
    @SerialName("barangay_label") val barangayLabel: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null,
    @SerialName("listed_at") val listedAt: String? = null,
    val images: List<ListingImage> = emptyList(),
    val amenities: List<ListingAmenity> = emptyList(),
)

@Serializable
data class ListingImage(
    val id: Int,
    val uuid: String,
    val url: String,
    @SerialName("sort_order") val sortOrder: Int,
)

@Serializable
data class ListingAmenity(
    val id: Int,
    val uuid: String,
    val name: String,
    val slug: String,
    val icon: String? = null,
)
