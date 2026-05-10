package ph.rentconnect.app.feature.contact.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.rentconnect.app.feature.contact.data.ContactRepository

class ContactViewModelFactory(
    private val repository: ContactRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ContactViewModel(repository) as T
}
