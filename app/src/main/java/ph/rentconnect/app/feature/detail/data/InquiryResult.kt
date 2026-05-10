package ph.rentconnect.app.feature.detail.data

sealed interface InquiryResult {
    data object Success : InquiryResult
    data class ValidationError(val errors: Map<String, List<String>>) : InquiryResult
    data object NotFound : InquiryResult
    data object Throttled : InquiryResult
    data object NetworkError : InquiryResult
    data object ServerError : InquiryResult
}
