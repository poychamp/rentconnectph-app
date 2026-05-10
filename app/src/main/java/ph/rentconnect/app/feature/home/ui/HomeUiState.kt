package ph.rentconnect.app.feature.home.ui

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.feature.home.data.Catalogs
import ph.rentconnect.app.feature.home.data.ListingCard

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val featured: List<ListingCard>,
        val recently: List<ListingCard>,
        val catalogs: Catalogs,
    ) : HomeUiState
    data class Error(val error: ApiError) : HomeUiState
}
