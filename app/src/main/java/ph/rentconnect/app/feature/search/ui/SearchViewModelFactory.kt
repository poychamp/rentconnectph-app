package ph.rentconnect.app.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.rentconnect.app.feature.search.data.SearchRepository

class SearchViewModelFactory(
    private val repository: SearchRepository,
    private val initialQuery: String? = null,
    private val initialArea: String? = null,
    private val initialType: String? = null,
    private val initialBudgetMin: Int? = null,
    private val initialBudgetMax: Int? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(
            repository = repository,
            initialQuery = initialQuery,
            initialArea = initialArea,
            initialType = initialType,
            initialBudgetMin = initialBudgetMin,
            initialBudgetMax = initialBudgetMax,
        ) as T
    }
}
