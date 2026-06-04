package ph.rentconnect.app.core.network

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val error: ApiError) : Result<Nothing>
}
