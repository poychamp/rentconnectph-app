package ph.rentconnect.app.feature.search.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
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

class SearchRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: SearchRepository

    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = SearchRepository(retrofit.create(SearchApi::class.java))
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it returns listings on successful browse`() = runTest {
        server.enqueue(MockResponse().setBody(BROWSE_RESPONSE).setResponseCode(200))

        val result = repository.search()

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(2, data.data!!.size)
        assertEquals("Durgan Drive Suites", data.data!![0].title)
        assertEquals("Janet Estates Suites", data.data!![1].title)
    }

    @Test
    fun `it maps pagination meta from response`() = runTest {
        server.enqueue(MockResponse().setBody(BROWSE_RESPONSE).setResponseCode(200))

        val result = repository.search()

        val meta = (result as Result.Success).data.meta!!
        assertEquals(1, meta.currentPage)
        assertEquals(5, meta.lastPage)
        assertEquals(24, meta.perPage)
        assertEquals(122, meta.total)
    }

    @Test
    fun `it passes all query parameters in request`() = runTest {
        server.enqueue(MockResponse().setBody(BROWSE_RESPONSE).setResponseCode(200))

        repository.search(
            query = "studio",
            area = "carmen",
            type = "apartment,studio",
            budgetMin = 10000,
            budgetMax = 20000,
            page = 2,
        )

        val request = server.takeRequest()
        val url = request.requestUrl!!
        assertEquals("/api/v1/search", url.encodedPath)
        assertEquals("studio", url.queryParameter("q"))
        assertEquals("carmen", url.queryParameter("area"))
        assertEquals("apartment,studio", url.queryParameter("type"))
        assertEquals("10000", url.queryParameter("budget_min"))
        assertEquals("20000", url.queryParameter("budget_max"))
        assertEquals("2", url.queryParameter("page"))
    }

    @Test
    fun `it returns empty list when no matches`() = runTest {
        server.enqueue(MockResponse().setBody(EMPTY_RESPONSE).setResponseCode(200))

        val result = repository.search(query = "nonexistent")

        assertTrue(result is Result.Success)
        val data = (result as Result.Success).data
        assertEquals(0, data.data!!.size)
        assertEquals(0, data.meta!!.total)
        assertEquals(1, data.meta!!.lastPage)
    }

    @Test
    fun `it maps filters echo from response`() = runTest {
        server.enqueue(MockResponse().setBody(FILTERED_RESPONSE).setResponseCode(200))

        val result = repository.search(query = "wifi", area = "carmen", budgetMin = 5000, budgetMax = 15000)

        val filters = (result as Result.Success).data.filters!!
        assertEquals("wifi", filters.q)
        assertEquals("carmen", filters.area)
        assertEquals(5000, filters.budgetMin)
        assertEquals(15000, filters.budgetMax)
    }

    @Test
    fun `it returns NetworkError on connection failure`() = runTest {
        server.shutdown()

        val result = repository.search()

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.NetworkError)
    }

    @Test
    fun `it returns ServerError on 500 response`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.search()

        assertTrue(result is Result.Failure)
        val error = (result as Result.Failure).error
        assertTrue(error is ApiError.ServerError)
        assertEquals(500, (error as ApiError.ServerError).code)
    }

    companion object {
        private val BROWSE_RESPONSE = """
        {
            "data": [
                {
                    "id": 1,
                    "uuid": "019de8ec-6be4-7274-b06e-0ebd430bdcd4",
                    "title": "Durgan Drive Suites",
                    "type": "studio",
                    "type_label": "Studio",
                    "price_monthly": 17678,
                    "beds": 1,
                    "baths": 1,
                    "sqm": 180,
                    "barangay": "uptown",
                    "barangay_label": "Uptown",
                    "image": "https://example.com/img1.jpg",
                    "image_count": 4,
                    "section": "verified"
                },
                {
                    "id": 2,
                    "uuid": "019de8ec-6bf5-703e-8887-a34fb99cc016",
                    "title": "Janet Estates Suites",
                    "type": "house",
                    "type_label": "House",
                    "price_monthly": 28990,
                    "beds": 3,
                    "baths": 1,
                    "sqm": 58,
                    "barangay": "others",
                    "barangay_label": "Others",
                    "image": "https://example.com/img2.jpg",
                    "image_count": 4,
                    "section": "verified"
                }
            ],
            "meta": {
                "current_page": 1,
                "last_page": 5,
                "per_page": 24,
                "total": 122
            },
            "filters": {
                "q": "",
                "area": null,
                "type": [],
                "budget_min": null,
                "budget_max": null
            },
            "catalogs": {
                "listing_types": [
                    {"value": "apartment", "label": "Apartment"},
                    {"value": "studio", "label": "Studio"}
                ],
                "barangays": [
                    {"value": "carmen", "label": "Carmen"},
                    {"value": "uptown", "label": "Uptown"}
                ]
            }
        }
        """.trimIndent()

        private val EMPTY_RESPONSE = """
        {
            "data": [],
            "meta": {
                "current_page": 1,
                "last_page": 1,
                "per_page": 24,
                "total": 0
            },
            "filters": {
                "q": "nonexistent",
                "area": null,
                "type": [],
                "budget_min": null,
                "budget_max": null
            },
            "catalogs": {
                "listing_types": [],
                "barangays": []
            }
        }
        """.trimIndent()

        private val FILTERED_RESPONSE = """
        {
            "data": [
                {
                    "id": 3,
                    "uuid": "019de8d4-9ab7-73b4-90ca-b439185d4e64",
                    "title": "Carmen Studio with WiFi",
                    "type": "studio",
                    "type_label": "Studio",
                    "price_monthly": 12000,
                    "beds": 1,
                    "baths": 1,
                    "sqm": 30,
                    "barangay": "carmen",
                    "barangay_label": "Carmen",
                    "image": "https://example.com/img3.jpg",
                    "image_count": 3,
                    "section": "verified"
                }
            ],
            "meta": {
                "current_page": 1,
                "last_page": 1,
                "per_page": 24,
                "total": 1
            },
            "filters": {
                "q": "wifi",
                "area": "carmen",
                "type": [],
                "budget_min": 5000,
                "budget_max": 15000
            },
            "catalogs": {
                "listing_types": [
                    {"value": "apartment", "label": "Apartment"},
                    {"value": "studio", "label": "Studio"}
                ],
                "barangays": [
                    {"value": "carmen", "label": "Carmen"}
                ]
            }
        }
        """.trimIndent()
    }
}
