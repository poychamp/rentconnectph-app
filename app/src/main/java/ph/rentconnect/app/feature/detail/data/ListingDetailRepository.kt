package ph.rentconnect.app.feature.detail.data

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import java.io.IOException

class ListingDetailRepository(private val api: ListingDetailApi) {

    suspend fun getListing(uuid: String): Result<ListingDetail> =
        try {
            val response = api.getListing(uuid)
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        Result.Success(body.listing)
                    } else {
                        Result.Failure(ApiError.Unknown(IllegalStateException("Empty body")))
                    }
                }
                response.code() == 404 -> Result.Failure(ApiError.NotFound)
                response.code() in 500..599 -> Result.Failure(ApiError.ServerError(response.code()))
                else -> Result.Failure(ApiError.Unknown(IllegalStateException("HTTP ${response.code()}")))
            }
        } catch (e: IOException) {
            Result.Failure(ApiError.NetworkError(e))
        } catch (e: Exception) {
            Result.Failure(ApiError.Unknown(e))
        }
}
