package ph.rentconnect.app.feature.search.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {
    @GET("/api/v1/search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("area") area: String? = null,
        @Query("type") type: String? = null,
        @Query("budget_min") budgetMin: Int? = null,
        @Query("budget_max") budgetMax: Int? = null,
        @Query("page") page: Int = 1,
    ): Response<SearchResponse>
}
