package ph.rentconnect.app.feature.detail.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListingDetailResponse(
    val listing: ListingDetail? = null,
)

@Serializable
data class ListingDetail(
    val id: Int,
    val uuid: String,
    val title: String,
    val type: String? = null,
    @SerialName("type_label") val typeLabel: String? = null,
    @SerialName("price_monthly") val priceMonthly: Int? = null,
    val beds: Int? = null,
    val baths: Int? = null,
    val sqm: Int? = null,
    val barangay: String? = null,
    @SerialName("barangay_label") val barangayLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val description: String? = null,
    @SerialName("verified_at") val verifiedAt: String? = null,
    @SerialName("listed_at") val listedAt: String? = null,
    val images: List<ListingImage>? = null,
    val amenities: List<ListingAmenity>? = null,
)

@Serializable
data class ListingImage(
    val id: Int,
    val uuid: String,
    val url: String? = null,
    @SerialName("sort_order") val sortOrder: Int? = null,
)

@Serializable
data class ListingAmenity(
    val id: Int,
    val uuid: String,
    val name: String? = null,
    val slug: String? = null,
    val icon: String? = null,
)
