package ph.rentconnect.app.feature.home.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val featured: List<ListingCard>,
    val recently: List<ListingCard>,
    val catalogs: Catalogs,
)

@Serializable
data class ListingCard(
    val id: Int,
    val uuid: String,
    val title: String,
    val type: String,
    @SerialName("type_label") val typeLabel: String,
    @SerialName("price_monthly") val priceMonthly: Int? = null,
    val beds: Int,
    val baths: Int,
    val sqm: Int,
    val barangay: String,
    @SerialName("barangay_label") val barangayLabel: String,
    val image: String? = null,
    @SerialName("image_count") val imageCount: Int,
    val section: String,
)

@Serializable
data class Catalogs(
    @SerialName("listing_types") val listingTypes: List<CatalogItem>,
    val barangays: List<CatalogItem>,
)

@Serializable
data class CatalogItem(
    val value: String,
    val label: String,
)
