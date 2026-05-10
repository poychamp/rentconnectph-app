package ph.rentconnect.app.feature.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.rentconnect.app.feature.search.data.SearchRepository

class SearchViewModelFactory(
    private val repository: SearchRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(repository) as T
    }
}
