package ph.rentconnect.app.core.network

sealed interface ApiError {
    data class NetworkError(val cause: Throwable) : ApiError
    data object NotFound : ApiError
    data class ServerError(val code: Int) : ApiError
    data class Unknown(val cause: Throwable) : ApiError
}
