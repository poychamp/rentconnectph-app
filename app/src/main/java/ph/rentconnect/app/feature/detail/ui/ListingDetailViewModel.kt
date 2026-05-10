package ph.rentconnect.app.feature.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.detail.data.ListingDetailRepository

class ListingDetailViewModel(
    private val uuid: String,
    private val repository: ListingDetailRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingDetailUiState>(ListingDetailUiState.Loading)
    val uiState: StateFlow<ListingDetailUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadListing()
    }

    fun retry() {
        loadListing()
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getListing(uuid)) {
                is Result.Success -> ListingDetailUiState.Success(result.data)
                is Result.Failure -> when (result.error) {
                    is ApiError.NotFound -> ListingDetailUiState.NotFound
                    else -> ListingDetailUiState.Error(result.error)
                }
            }
            _isRefreshing.value = false
        }
    }

    private fun loadListing() {
        _uiState.value = ListingDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getListing(uuid)) {
                is Result.Success -> ListingDetailUiState.Success(result.data)
                is Result.Failure -> when (result.error) {
                    is ApiError.NotFound -> ListingDetailUiState.NotFound
                    else -> ListingDetailUiState.Error(result.error)
                }
            }
        }
    }
}
