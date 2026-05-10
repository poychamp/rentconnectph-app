package ph.rentconnect.app.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.search.data.SearchRepository

private const val DEBOUNCE_MS = 1000L

class SearchViewModel(
    private val repository: SearchRepository,
    initialQuery: String? = null,
    initialArea: String? = null,
    initialType: String? = null,
    initialBudgetMin: Int? = null,
    initialBudgetMax: Int? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(
            searchQuery = initialQuery ?: "",
            selectedArea = initialArea,
            selectedTypes = initialType?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            budgetMin = initialBudgetMin,
            budgetMax = initialBudgetMax,
        ),
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Timer 1: q + area + budget
    private var searchJob: Job? = null
    // Timer 2: type chips
    private var typeJob: Job? = null

    init {
        viewModelScope.launch { load() }
    }

    // --- Search timer group (q, area, budget) ---

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        scheduleSearch()
    }

    fun onAreaChange(area: String?) {
        _uiState.update { it.copy(selectedArea = area) }
        scheduleSearch()
    }

    fun onBudgetChange(min: Int?, max: Int?) {
        _uiState.update { it.copy(budgetMin = min, budgetMax = max) }
        scheduleSearch()
    }

    /** Enter key or Search button — fire immediately, cancel pending timer. */
    fun submitNow() {
        searchJob?.cancel()
        typeJob?.cancel()
        viewModelScope.launch { load() }
    }

    // --- Type timer group ---

    fun onTypeToggle(type: String) {
        _uiState.update { state ->
            val types = if (type in state.selectedTypes) {
                state.selectedTypes - type
            } else {
                state.selectedTypes + type
            }
            state.copy(selectedTypes = types)
        }
        scheduleTypeSearch()
    }

    // --- Shared ---

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedArea = null,
                selectedTypes = emptyList(),
                budgetMin = null,
                budgetMax = null,
            )
        }
        searchJob?.cancel()
        typeJob?.cancel()
        viewModelScope.launch { load() }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage >= state.lastPage) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, error = null) }

            val nextPage = state.currentPage + 1
            val result = repository.search(
                query = state.searchQuery.ifBlank { null },
                area = state.selectedArea,
                type = state.selectedTypes.takeIf { it.isNotEmpty() }?.joinToString(","),
                budgetMin = state.budgetMin,
                budgetMax = state.budgetMax,
                page = nextPage,
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            items = it.items + result.data.data,
                            currentPage = result.data.meta.currentPage,
                            lastPage = result.data.meta.lastPage,
                            total = result.data.meta.total,
                            isLoadingMore = false,
                            error = null,
                        )
                    }
                }
                is Result.Failure -> {
                    _uiState.update { it.copy(isLoadingMore = false, error = result.error) }
                }
            }
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        searchJob?.cancel()
        typeJob?.cancel()
        viewModelScope.launch {
            load()
            _isRefreshing.value = false
        }
    }

    // --- Private ---

    private fun scheduleSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            load()
        }
    }

    private fun scheduleTypeSearch() {
        typeJob?.cancel()
        typeJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            load()
        }
    }

    private suspend fun load() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        val result = repository.search(
            query = state.searchQuery.ifBlank { null },
            area = state.selectedArea,
            type = state.selectedTypes.takeIf { it.isNotEmpty() }?.joinToString(","),
            budgetMin = state.budgetMin,
            budgetMax = state.budgetMax,
            page = 1,
        )

        when (result) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        items = result.data.data,
                        currentPage = result.data.meta.currentPage,
                        lastPage = result.data.meta.lastPage,
                        total = result.data.meta.total,
                        catalogs = result.data.catalogs,
                        isLoading = false,
                        error = null,
                    )
                }
            }
            is Result.Failure -> {
                _uiState.update { it.copy(isLoading = false, error = result.error) }
            }
        }
    }
}
