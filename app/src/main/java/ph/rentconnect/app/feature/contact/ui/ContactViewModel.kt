package ph.rentconnect.app.feature.contact.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.rentconnect.app.feature.contact.data.ContactRepository
import ph.rentconnect.app.feature.contact.data.ContactResult

class ContactViewModel(
    private val repository: ContactRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactUiState>(ContactUiState.Form())
    val uiState: StateFlow<ContactUiState> = _uiState

    fun updateName(name: String) {
        val current = _uiState.value
        if (current is ContactUiState.Form) {
            _uiState.value = current.copy(name = name)
        }
    }

    fun updateEmail(email: String) {
        val current = _uiState.value
        if (current is ContactUiState.Form) {
            _uiState.value = current.copy(email = email)
        }
    }

    fun updateMessage(message: String) {
        val current = _uiState.value
        if (current is ContactUiState.Form) {
            _uiState.value = current.copy(message = message)
        }
    }

    fun onFieldFocus(field: String) {
        val current = _uiState.value
        if (current is ContactUiState.Form) {
            _uiState.value = current.copy(
                fieldErrors = current.fieldErrors - field,
                submitError = null,
            )
        }
    }

    fun onFieldBlur(field: String) {
        val current = _uiState.value
        if (current !is ContactUiState.Form) return
        val error = when (field) {
            "name" -> validateName(current.name)
            "email" -> validateEmail(current.email)
            "message" -> validateMessage(current.message)
            else -> null
        }
        if (error != null) {
            _uiState.value = current.copy(
                fieldErrors = current.fieldErrors + (field to listOf(error)),
            )
        }
    }

    fun submitContact() {
        val current = _uiState.value
        if (current !is ContactUiState.Form || current.isSubmitting) return

        val clientErrors = validateAllFields(current.name, current.email, current.message)
        if (clientErrors.isNotEmpty()) {
            _uiState.value = current.copy(fieldErrors = clientErrors, submitError = null)
            return
        }

        _uiState.value = current.copy(isSubmitting = true, fieldErrors = emptyMap(), submitError = null)
        viewModelScope.launch {
            _uiState.value = when (val result = repository.sendMessage(current.name, current.email, current.message)) {
                is ContactResult.Success -> ContactUiState.Success
                is ContactResult.ValidationError -> current.copy(
                    isSubmitting = false,
                    fieldErrors = result.errors,
                )
                is ContactResult.Throttled -> current.copy(
                    isSubmitting = false,
                    submitError = "Too many messages. Please try again later.",
                )
                is ContactResult.NetworkError -> current.copy(
                    isSubmitting = false,
                    submitError = "Connection error. Please check your internet and try again.",
                )
                is ContactResult.ServerError -> current.copy(
                    isSubmitting = false,
                    submitError = "Something went wrong. Please try again.",
                )
            }
        }
    }

    companion object {
        fun validateName(name: String): String? = when {
            name.trim().isEmpty() -> "Name is required."
            name.trim().length > 120 -> "Name must be 120 characters or fewer."
            else -> null
        }

        fun validateEmail(email: String): String? = when {
            email.isBlank() -> "Email is required."
            email.trim().length > 255 -> "Email must be 255 characters or fewer."
            !isValidEmail(email.trim()) -> "Please enter a valid email address."
            else -> null
        }

        fun validateMessage(message: String): String? = when {
            message.trim().isEmpty() -> "Message is required."
            message.trim().length > 5000 -> "Message must be 5000 characters or fewer."
            else -> null
        }

        fun validateAllFields(name: String, email: String, message: String): Map<String, List<String>> {
            val errors = mutableMapOf<String, List<String>>()
            validateName(name)?.let { errors["name"] = listOf(it) }
            validateEmail(email)?.let { errors["email"] = listOf(it) }
            validateMessage(message)?.let { errors["message"] = listOf(it) }
            return errors
        }

        fun isValidEmail(email: String): Boolean =
            email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
    }
}
