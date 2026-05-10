package ph.rentconnect.app.feature.detail.ui

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.feature.detail.data.ListingDetail

sealed interface ListingDetailUiState {
    data object Loading : ListingDetailUiState
    data class Success(val listing: ListingDetail) : ListingDetailUiState
    data object NotFound : ListingDetailUiState
    data class Error(val error: ApiError) : ListingDetailUiState
}
