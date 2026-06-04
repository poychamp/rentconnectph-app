package ph.rentconnect.app.feature.contact.data

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

class ContactRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ContactRepository
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = ContactRepository(retrofit.create(ContactApi::class.java), json)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it returns Success on 200 and sends correct request shape`() = runTest {
        server.enqueue(MockResponse().setBody("""{"success":true}""").setResponseCode(200))

        val result = repository.sendMessage("Maria Santos", "maria@example.com", "I want to list a property.")

        assertTrue(result is ContactResult.Success)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        val body = request.body.readUtf8()
        assertTrue(body.contains("\"name\":\"Maria Santos\""))
        assertTrue(body.contains("\"email\":\"maria@example.com\""))
        assertTrue(body.contains("\"message\":\"I want to list a property.\""))
    }

    @Test
    fun `it returns ValidationError with multiple field errors on 422`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setBody(
                    """{
                        "message":"Name is required.",
                        "errors":{
                            "name":["Name is required."],
                            "email":["Please enter a valid email address."],
                            "message":["Message is required."]
                        }
                    }""",
                ),
        )

        val result = repository.sendMessage("", "nope", "")

        assertTrue(result is ContactResult.ValidationError)
        val errors = (result as ContactResult.ValidationError).errors
        assertEquals(listOf("Name is required."), errors["name"])
        assertEquals(listOf("Please enter a valid email address."), errors["email"])
        assertEquals(listOf("Message is required."), errors["message"])
    }

    @Test
    fun `it returns ValidationError with single field error on 422`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(422)
                .setBody(
                    """{
                        "message":"Email must be 255 characters or fewer.",
                        "errors":{
                            "email":["Email must be 255 characters or fewer."]
                        }
                    }""",
                ),
        )

        val result = repository.sendMessage("Maria", "a".repeat(256) + "@x.com", "Hello")

        assertTrue(result is ContactResult.ValidationError)
        val errors = (result as ContactResult.ValidationError).errors
        assertEquals(listOf("Email must be 255 characters or fewer."), errors["email"])
    }

    @Test
    fun `it returns Throttled on 429`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429))

        val result = repository.sendMessage("Maria", "maria@example.com", "Hi")

        assertTrue(result is ContactResult.Throttled)
    }

    @Test
    fun `it returns NetworkError on connection failure`() = runTest {
        server.shutdown()

        val result = repository.sendMessage("Maria", "maria@example.com", "Hi")

        assertTrue(result is ContactResult.NetworkError)
    }

    @Test
    fun `it returns ServerError on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.sendMessage("Maria", "maria@example.com", "Hi")

        assertTrue(result is ContactResult.ServerError)
    }
}
