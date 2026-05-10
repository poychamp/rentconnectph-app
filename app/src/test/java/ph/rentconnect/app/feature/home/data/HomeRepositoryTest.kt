package ph.rentconnect.app.feature.home.data

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
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class HomeRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: HomeRepository

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(HomeApi::class.java)
        repository = HomeRepository(api)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it returns featured and recently verified listings on success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

        val result = repository.getHome()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(1, data.featured.size)
        assertEquals(1, data.recently.size)

        val featured = data.featured[0]
        assertEquals("abc-123", featured.uuid)
        assertEquals("Larkin Harbor Suites", featured.title)
        assertEquals("apartment", featured.type)
        assertEquals("Apartment", featured.typeLabel)
        assertEquals(64000, featured.priceMonthly)
        assertEquals(1, featured.beds)
        assertEquals(2, featured.baths)
        assertEquals(29, featured.sqm)
        assertEquals("uptown", featured.barangay)
        assertEquals("Uptown", featured.barangayLabel)
        assertEquals("https://example.com/image1.jpg", featured.image)
        assertEquals(4, featured.imageCount)
        assertEquals("verified", featured.section)
    }

    @Test
    fun `it returns catalogs with listing types and barangays`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

        val result = repository.getHome()

        assertTrue(result is Result.Success)
        val catalogs = (result as Result.Success).data.catalogs
        assertEquals(2, catalogs.listingTypes.size)
        assertEquals("apartment", catalogs.listingTypes[0].value)
        assertEquals("Apartment", catalogs.listingTypes[0].label)
        assertEquals(2, catalogs.barangays.size)
        assertEquals("uptown", catalogs.barangays[0].value)
        assertEquals("Uptown", catalogs.barangays[0].label)
    }

    @Test
    fun `it returns empty lists when no listings exist`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(EMPTY_BODY))

        val result = repository.getHome()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertTrue(data.featured.isEmpty())
        assertTrue(data.recently.isEmpty())
        assertEquals(2, data.catalogs.listingTypes.size)
    }

    @Test
    fun `it returns NetworkError on connection failure`() = runTest {
        server.shutdown()

        val result = repository.getHome()

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.NetworkError)
    }

    @Test
    fun `it returns ServerError on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getHome()

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.ServerError)
    }

    companion object {
        private val SUCCESS_BODY = """
            {
                "featured": [
                    {
                        "id": 1,
                        "uuid": "abc-123",
                        "title": "Larkin Harbor Suites",
                        "type": "apartment",
                        "type_label": "Apartment",
                        "price_monthly": 64000,
                        "beds": 1,
                        "baths": 2,
                        "sqm": 29,
                        "barangay": "uptown",
                        "barangay_label": "Uptown",
                        "image": "https://example.com/image1.jpg",
                        "image_count": 4,
                        "section": "verified"
                    }
                ],
                "recently": [
                    {
                        "id": 2,
                        "uuid": "def-456",
                        "title": "Durgan Drive Suites",
                        "type": "studio",
                        "type_label": "Studio",
                        "price_monthly": 17678,
                        "beds": 1,
                        "baths": 1,
                        "sqm": 180,
                        "barangay": "uptown",
                        "barangay_label": "Uptown",
                        "image": "https://example.com/image2.jpg",
                        "image_count": 4,
                        "section": "recently"
                    }
                ],
                "catalogs": {
                    "listing_types": [
                        {"value": "apartment", "label": "Apartment"},
                        {"value": "studio", "label": "Studio"}
                    ],
                    "barangays": [
                        {"value": "uptown", "label": "Uptown"},
                        {"value": "lapasan", "label": "Lapasan"}
                    ]
                }
            }
        """.trimIndent()

        private val EMPTY_BODY = """
            {
                "featured": [],
                "recently": [],
                "catalogs": {
                    "listing_types": [
                        {"value": "apartment", "label": "Apartment"},
                        {"value": "studio", "label": "Studio"}
                    ],
                    "barangays": [
                        {"value": "uptown", "label": "Uptown"},
                        {"value": "lapasan", "label": "Lapasan"}
                    ]
                }
            }
        """.trimIndent()
    }
}
