package ph.rentconnect.app.feature.detail.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class InquiryRepository(
    private val api: InquiryApi,
    private val json: Json,
) {

    suspend fun submitInquiry(uuid: String, name: String, phone: String): InquiryResult =
        try {
            val requestBody = json.encodeToString(
                InquiryRequest.serializer(),
                InquiryRequest(name = name, phone = phone),
            )
            val response = api.submitInquiry(
                uuid = uuid,
                body = requestBody.toRequestBody("application/json".toMediaType()),
            )
            when (response.code()) {
                200 -> InquiryResult.Success
                404 -> InquiryResult.NotFound
                422 -> {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val parsed = json.decodeFromString(
                            InquiryValidationErrorResponse.serializer(),
                            errorBody,
                        )
                        InquiryResult.ValidationError(parsed.errors)
                    } else {
                        InquiryResult.ValidationError(emptyMap())
                    }
                }
                429 -> InquiryResult.Throttled
                in 500..599 -> InquiryResult.ServerError
                else -> InquiryResult.ServerError
            }
        } catch (_: IOException) {
            InquiryResult.NetworkError
        } catch (_: Exception) {
            InquiryResult.ServerError
        }
}
