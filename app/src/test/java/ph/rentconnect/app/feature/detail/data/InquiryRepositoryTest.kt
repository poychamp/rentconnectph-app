package ph.rentconnect.app.feature.detail.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class InquiryRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: InquiryRepository

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(InquiryApi::class.java)
        repository = InquiryRepository(api, json)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it returns Success on 200`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"success":true}"""))

        val result = repository.submitInquiry(LISTING_UUID, "Maria Cruz", "09171234567")

        assertTrue(result is InquiryResult.Success)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertTrue(request.path!!.contains("/api/v1/listings/$LISTING_UUID/inquiries"))
        val body = request.body.readUtf8()
        assertTrue(body.contains(""""name":"Maria Cruz""""))
        assertTrue(body.contains(""""phone":"09171234567""""))
    }

    @Test
    fun `it returns ValidationError on 422 with field errors`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(422).setBody(
                """
                {
                    "message": "The name field is required.",
                    "errors": {
                        "name": ["Name is required."],
                        "phone": ["Invalid PH mobile number."]
                    }
                }
                """.trimIndent()
            )
        )

        val result = repository.submitInquiry(LISTING_UUID, "", "12345")

        assertTrue(result is InquiryResult.ValidationError)
        val errors = (result as InquiryResult.ValidationError).errors
        assertEquals(listOf("Name is required."), errors["name"])
        assertEquals(listOf("Invalid PH mobile number."), errors["phone"])
    }

    @Test
    fun `it returns ValidationError with single field error`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(422).setBody(
                """
                {
                    "message": "Name must be 120 characters or fewer.",
                    "errors": {
                        "name": ["Name must be 120 characters or fewer."]
                    }
                }
                """.trimIndent()
            )
        )

        val result = repository.submitInquiry(LISTING_UUID, "A".repeat(121), "09171234567")

        assertTrue(result is InquiryResult.ValidationError)
        val errors = (result as InquiryResult.ValidationError).errors
        assertEquals(listOf("Name must be 120 characters or fewer."), errors["name"])
        assertTrue(errors["phone"] == null)
    }

    @Test
    fun `it returns NotFound on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository.submitInquiry("nonexistent-uuid", "Maria Cruz", "09171234567")

        assertTrue(result is InquiryResult.NotFound)
    }

    @Test
    fun `it returns Throttled on 429`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))

        val result = repository.submitInquiry(LISTING_UUID, "Maria Cruz", "09171234567")

        assertTrue(result is InquiryResult.Throttled)
    }

    @Test
    fun `it returns NetworkError on connection failure`() = runTest {
        server.shutdown()

        val result = repository.submitInquiry(LISTING_UUID, "Maria Cruz", "09171234567")

        assertTrue(result is InquiryResult.NetworkError)
    }

    @Test
    fun `it returns ServerError on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.submitInquiry(LISTING_UUID, "Maria Cruz", "09171234567")

        assertTrue(result is InquiryResult.ServerError)
    }

    companion object {
        private const val LISTING_UUID = "019de8d4-a590-7194-b591-43298a88378f"
    }
}
