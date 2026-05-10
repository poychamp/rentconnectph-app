package ph.rentconnect.app.feature.contact.ui

sealed interface ContactUiState {
    data class Form(
        val name: String = "",
        val email: String = "",
        val message: String = "",
        val isSubmitting: Boolean = false,
        val fieldErrors: Map<String, List<String>> = emptyMap(),
        val submitError: String? = null,
    ) : ContactUiState

    data object Success : ContactUiState
}
