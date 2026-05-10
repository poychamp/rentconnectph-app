package ph.rentconnect.app.feature.detail.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ListingDetailApi {

    @GET("/api/v1/listings/{uuid}")
    suspend fun getListing(@Path("uuid") uuid: String): Response<ListingDetailResponse>
}
