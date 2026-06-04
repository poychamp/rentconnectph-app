package ph.rentconnect.app.feature.home.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val featured: List<ListingCard>? = null,
    val recently: List<ListingCard>? = null,
    val catalogs: Catalogs? = null,
)

@Serializable
data class ListingCard(
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
    val image: String? = null,
    @SerialName("image_count") val imageCount: Int? = null,
    val section: String? = null,
)

@Serializable
data class Catalogs(
    @SerialName("listing_types") val listingTypes: List<CatalogItem>? = null,
    val barangays: List<CatalogItem>? = null,
)

@Serializable
data class CatalogItem(
    val value: String? = null,
    val label: String? = null,
)
