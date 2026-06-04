package ph.rentconnect.app.feature.search.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.feature.home.data.ListingCard

@Serializable
data class SearchResponse(
    val data: List<ListingCard>? = null,
    val meta: PaginationMeta? = null,
    val filters: FiltersEcho? = null,
    val catalogs: SearchCatalogs? = null,
)

@Serializable
data class PaginationMeta(
    @SerialName("current_page") val currentPage: Int? = null,
    @SerialName("last_page") val lastPage: Int? = null,
    @SerialName("per_page") val perPage: Int? = null,
    val total: Int? = null,
)

@Serializable
data class FiltersEcho(
    val q: String? = null,
    val area: String? = null,
    val type: List<String>? = null,
    @SerialName("budget_min") val budgetMin: Int? = null,
    @SerialName("budget_max") val budgetMax: Int? = null,
)

@Serializable
data class SearchCatalogs(
    @SerialName("listing_types") val listingTypes: List<CatalogItem>? = null,
    val barangays: List<CatalogItem>? = null,
)
