package ph.rentconnect.app.feature.detail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.detail.data.InquiryRepository
import ph.rentconnect.app.feature.detail.data.InquiryResult
import ph.rentconnect.app.feature.detail.data.ListingDetailRepository

class ListingDetailViewModel(
    private val uuid: String,
    private val repository: ListingDetailRepository,
    private val inquiryRepository: InquiryRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ListingDetailUiState>(ListingDetailUiState.Loading)
    val uiState: StateFlow<ListingDetailUiState> = _uiState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _inquiryState = MutableStateFlow<InquiryDialogState>(InquiryDialogState.Hidden)
    val inquiryState: StateFlow<InquiryDialogState> = _inquiryState

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

    fun openInquiryDialog() {
        _inquiryState.value = InquiryDialogState.Visible()
    }

    fun closeInquiryDialog() {
        _inquiryState.value = InquiryDialogState.Hidden
    }

    fun updateInquiryName(name: String) {
        val current = _inquiryState.value
        if (current is InquiryDialogState.Visible) {
            _inquiryState.value = current.copy(name = name)
        }
    }

    fun updateInquiryPhone(phone: String) {
        val current = _inquiryState.value
        if (current is InquiryDialogState.Visible) {
            _inquiryState.value = current.copy(phone = phone)
        }
    }

    fun onFieldFocus(field: String) {
        val current = _inquiryState.value
        if (current is InquiryDialogState.Visible) {
            _inquiryState.value = current.copy(
                fieldErrors = current.fieldErrors - field,
                submitError = null,
            )
        }
    }

    fun onFieldBlur(field: String) {
        val current = _inquiryState.value
        if (current !is InquiryDialogState.Visible) return
        val error = when (field) {
            "name" -> validateName(current.name)
            "phone" -> validatePhone(current.phone)
            else -> null
        }
        if (error != null) {
            _inquiryState.value = current.copy(
                fieldErrors = current.fieldErrors + (field to listOf(error)),
            )
        }
    }

    fun submitInquiry() {
        val current = _inquiryState.value
        if (current !is InquiryDialogState.Visible || current.isSubmitting) return
        val repo = inquiryRepository ?: return

        val clientErrors = validateInquiryFields(current.name, current.phone)
        if (clientErrors.isNotEmpty()) {
            _inquiryState.value = current.copy(fieldErrors = clientErrors, submitError = null)
            return
        }

        _inquiryState.value = current.copy(isSubmitting = true, fieldErrors = emptyMap(), submitError = null)
        viewModelScope.launch {
            _inquiryState.value = when (val result = repo.submitInquiry(uuid, current.name, current.phone)) {
                is InquiryResult.Success -> InquiryDialogState.Success(result.contactInfo)
                is InquiryResult.ValidationError -> current.copy(
                    isSubmitting = false,
                    fieldErrors = result.errors,
                )
                is InquiryResult.Throttled -> current.copy(
                    isSubmitting = false,
                    submitError = "Too many inquiries. Please try again later.",
                )
                is InquiryResult.NotFound -> current.copy(
                    isSubmitting = false,
                    submitError = "This listing is no longer available.",
                )
                is InquiryResult.NetworkError -> current.copy(
                    isSubmitting = false,
                    submitError = "Connection error. Please check your internet and try again.",
                )
                is InquiryResult.ServerError -> current.copy(
                    isSubmitting = false,
                    submitError = "Something went wrong. Please try again.",
                )
            }
        }
    }

    companion object {
        fun validateName(name: String): String? = when {
            name.isBlank() -> "Name is required."
            name.length > 120 -> "Name must be 120 characters or fewer."
            else -> null
        }

        fun validatePhone(phone: String): String? = when {
            phone.isBlank() -> "Phone is required."
            !isValidPhMobile(phone) -> "Invalid PH mobile number."
            else -> null
        }

        fun validateInquiryFields(name: String, phone: String): Map<String, List<String>> {
            val errors = mutableMapOf<String, List<String>>()
            validateName(name)?.let { errors["name"] = listOf(it) }
            validatePhone(phone)?.let { errors["phone"] = listOf(it) }
            return errors
        }

        fun isValidPhMobile(raw: String): Boolean {
            val digits = raw.replace(Regex("\\D"), "")
            return when {
                digits.length == 12 && digits.startsWith("63") && digits[2] == '9' -> true
                digits.length == 11 && digits.startsWith("09") -> true
                digits.length == 10 && digits.startsWith("9") -> true
                else -> false
            }
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
