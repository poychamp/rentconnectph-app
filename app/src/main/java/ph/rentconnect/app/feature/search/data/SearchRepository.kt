package ph.rentconnect.app.feature.search.data

import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import java.io.IOException

class SearchRepository(private val api: SearchApi) {

    suspend fun search(
        query: String? = null,
        area: String? = null,
        type: String? = null,
        budgetMin: Int? = null,
        budgetMax: Int? = null,
        page: Int = 1,
    ): Result<SearchResponse> = try {
        val response = api.search(query, area, type, budgetMin, budgetMax, page)
        when {
            response.isSuccessful -> Result.Success(response.body()!!)
            response.code() in 500..599 -> Result.Failure(ApiError.ServerError(response.code()))
            else -> Result.Failure(ApiError.Unknown(Exception("HTTP ${response.code()}")))
        }
    } catch (e: IOException) {
        Result.Failure(ApiError.NetworkError(e))
    } catch (e: Exception) {
        Result.Failure(ApiError.Unknown(e))
    }
}
