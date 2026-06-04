package ph.rentconnect.app.feature.home.data

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import java.io.IOException

class HomeRepository(private val api: HomeApi) {

    suspend fun getHome(): Result<HomeResponse> =
        try {
            val response = api.getHome()
            when {
                response.isSuccessful -> {
                    val body = response.body()
                    if (body != null) {
                        Result.Success(body)
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
