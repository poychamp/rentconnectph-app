package ph.rentconnect.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.home.data.HomeRepository

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHome()
    }

    fun refresh() {
        loadHome()
    }

    private fun loadHome() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = repository.getHome()) {
                is Result.Success -> HomeUiState.Success(
                    featured = result.data.featured,
                    recently = result.data.recently,
                    catalogs = result.data.catalogs,
                )
                is Result.Failure -> HomeUiState.Error(result.error)
            }
        }
    }
}
