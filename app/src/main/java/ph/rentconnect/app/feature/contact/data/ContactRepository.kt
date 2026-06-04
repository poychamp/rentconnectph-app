package ph.rentconnect.app.feature.contact.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class ContactRepository(
    private val api: ContactApi,
    private val json: Json,
) {

    suspend fun sendMessage(name: String, email: String, message: String): ContactResult =
        try {
            val requestBody = json.encodeToString(
                ContactRequest.serializer(),
                ContactRequest(name = name, email = email, message = message),
            )
            val response = api.sendMessage(
                body = requestBody.toRequestBody("application/json".toMediaType()),
            )
            when (response.code()) {
                200 -> ContactResult.Success
                422 -> {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val parsed = json.decodeFromString(
                            ContactValidationErrorResponse.serializer(),
                            errorBody,
                        )
                        ContactResult.ValidationError(parsed.errors)
                    } else {
                        ContactResult.ValidationError(emptyMap())
                    }
                }
                429 -> ContactResult.Throttled
                in 500..599 -> ContactResult.ServerError
                else -> ContactResult.ServerError
            }
        } catch (_: IOException) {
            ContactResult.NetworkError
        } catch (_: Exception) {
            ContactResult.ServerError
        }
}
