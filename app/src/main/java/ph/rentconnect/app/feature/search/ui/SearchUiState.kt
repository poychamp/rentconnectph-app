package ph.rentconnect.app.feature.search.ui

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.feature.home.data.ListingCard
import ph.rentconnect.app.feature.search.data.SearchCatalogs

data class SearchUiState(
    val items: List<ListingCard> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val lastPage: Int = 1,
    val total: Int = 0,
    val error: ApiError? = null,
    val searchQuery: String = "",
    val selectedArea: String? = null,
    val selectedTypes: List<String> = emptyList(),
    val budgetMin: Int? = null,
    val budgetMax: Int? = null,
    val catalogs: SearchCatalogs? = null,
) {
    val hasMore: Boolean get() = currentPage < lastPage
}
