package ph.rentconnect.app.feature.contact.data

sealed interface ContactResult {
    data object Success : ContactResult
    data class ValidationError(val errors: Map<String, List<String>>) : ContactResult
    data object Throttled : ContactResult
    data object NetworkError : ContactResult
    data object ServerError : ContactResult
}
