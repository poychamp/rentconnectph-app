package ph.rentconnect.app.feature.detail.ui

sealed interface InquiryDialogState {
    data object Hidden : InquiryDialogState

    data class Visible(
        val name: String = "",
        val phone: String = "",
        val isSubmitting: Boolean = false,
        val fieldErrors: Map<String, List<String>> = emptyMap(),
        val submitError: String? = null,
    ) : InquiryDialogState

    data object Success : InquiryDialogState
}
