package ph.rentconnect.app.feature.home.data

import retrofit2.Response
import retrofit2.http.GET

interface HomeApi {
    @GET("api/v1/home")
    suspend fun getHome(): Response<HomeResponse>
}
