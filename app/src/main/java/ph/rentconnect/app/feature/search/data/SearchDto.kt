package ph.rentconnect.app.feature.search.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.feature.home.data.ListingCard

@Serializable
data class SearchResponse(
    val data: List<ListingCard>,
    val meta: PaginationMeta,
    val filters: FiltersEcho,
    val catalogs: SearchCatalogs,
)

@Serializable
data class PaginationMeta(
    @SerialName("current_page") val currentPage: Int,
    @SerialName("last_page") val lastPage: Int,
    @SerialName("per_page") val perPage: Int,
    val total: Int,
)

@Serializable
data class FiltersEcho(
    val q: String = "",
    val area: String? = null,
    val type: List<String> = emptyList(),
    @SerialName("budget_min") val budgetMin: Int? = null,
    @SerialName("budget_max") val budgetMax: Int? = null,
)

@Serializable
data class SearchCatalogs(
    @SerialName("listing_types") val listingTypes: List<CatalogItem>,
    val barangays: List<CatalogItem>,
)
