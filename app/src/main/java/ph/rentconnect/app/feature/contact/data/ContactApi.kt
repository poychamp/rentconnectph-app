package ph.rentconnect.app.feature.contact.data

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ContactApi {

    @POST("/api/v1/contact")
    suspend fun sendMessage(@Body body: RequestBody): Response<Unit>
}
