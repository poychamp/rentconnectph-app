package ph.rentconnect.app.feature.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.rentconnect.app.feature.detail.data.ListingDetailRepository

class ListingDetailViewModelFactory(
    private val uuid: String,
    private val repository: ListingDetailRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ListingDetailViewModel(uuid, repository) as T
}
