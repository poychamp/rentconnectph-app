package ph.rentconnect.app.feature.detail.data

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ListingDetailRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: ListingDetailRepository

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(ListingDetailApi::class.java)
        repository = ListingDetailRepository(api)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it maps full listing response on success`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

        val result = repository.getListing("019de8d4-9ab7-73b4-90ca-b439185d4e64")

        assertTrue(result is Result.Success)
        val listing = (result as Result.Success).data
        assertEquals("019de8d4-9ab7-73b4-90ca-b439185d4e64", listing.uuid)
        assertEquals("Larkin Harbor Suites", listing.title)
        assertEquals("apartment", listing.type)
        assertEquals("Apartment", listing.typeLabel)
        assertEquals(64000, listing.priceMonthly)
        assertEquals(1, listing.beds)
        assertEquals(2, listing.baths)
        assertEquals(29, listing.sqm)
        assertEquals("uptown", listing.barangay)
        assertEquals("Uptown", listing.barangayLabel)
        assertEquals(8.15, listing.latitude)
        assertEquals(124.96, listing.longitude)
        assertEquals("Modern minimalist finish.", listing.description)
        assertEquals("2026-05-01T00:00:00+08:00", listing.verifiedAt)
        assertEquals("2026-04-20T00:00:00+08:00", listing.listedAt)
    }

    @Test
    fun `it maps images in order`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

        val result = repository.getListing("019de8d4-9ab7-73b4-90ca-b439185d4e64")

        val images = (result as Result.Success).data.images!!
        assertEquals(2, images.size)
        assertEquals("https://cdn.example.com/img1.jpg", images[0].url)
        assertEquals(1, images[0].sortOrder)
        assertEquals("https://cdn.example.com/img2.jpg", images[1].url)
        assertEquals(2, images[1].sortOrder)
    }

    @Test
    fun `it maps amenities`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(SUCCESS_BODY))

        val result = repository.getListing("019de8d4-9ab7-73b4-90ca-b439185d4e64")

        val amenities = (result as Result.Success).data.amenities!!
        assertEquals(2, amenities.size)
        assertEquals("Parking", amenities[0].name)
        assertEquals("parking", amenities[0].slug)
        assertEquals("Wi-Fi", amenities[1].name)
        assertEquals("wi-fi", amenities[1].slug)
    }

    @Test
    fun `it handles nullable coordinates and description`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(NULLABLE_FIELDS_BODY))

        val result = repository.getListing("019de8d4-0000-0000-0000-000000000000")

        assertTrue(result is Result.Success)
        val listing = (result as Result.Success).data
        assertNull(listing.latitude)
        assertNull(listing.longitude)
        assertNull(listing.description)
        assertNull(listing.verifiedAt)
        assertNull(listing.listedAt)
    }

    @Test
    fun `it returns NotFound on 404`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository.getListing("nonexistent-uuid")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.NotFound)
    }

    @Test
    fun `it returns NetworkError on connection failure`() = runTest {
        server.shutdown()

        val result = repository.getListing("019de8d4-9ab7-73b4-90ca-b439185d4e64")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.NetworkError)
    }

    @Test
    fun `it returns ServerError on 500`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getListing("019de8d4-9ab7-73b4-90ca-b439185d4e64")

        assertTrue(result is Result.Failure)
        assertTrue((result as Result.Failure).error is ApiError.ServerError)
    }

    companion object {
        private val SUCCESS_BODY = """
            {
                "listing": {
                    "id": 1,
                    "uuid": "019de8d4-9ab7-73b4-90ca-b439185d4e64",
                    "title": "Larkin Harbor Suites",
                    "type": "apartment",
                    "type_label": "Apartment",
                    "price_monthly": 64000,
                    "beds": 1,
                    "baths": 2,
                    "sqm": 29,
                    "barangay": "uptown",
                    "barangay_label": "Uptown",
                    "latitude": 8.15,
                    "longitude": 124.96,
                    "description": "Modern minimalist finish.",
                    "verified_at": "2026-05-01T00:00:00+08:00",
                    "listed_at": "2026-04-20T00:00:00+08:00",
                    "images": [
                        {
                            "id": 10,
                            "uuid": "img-uuid-1",
                            "url": "https://cdn.example.com/img1.jpg",
                            "sort_order": 1
                        },
                        {
                            "id": 11,
                            "uuid": "img-uuid-2",
                            "url": "https://cdn.example.com/img2.jpg",
                            "sort_order": 2
                        }
                    ],
                    "amenities": [
                        {
                            "id": 1,
                            "uuid": "amen-uuid-1",
                            "name": "Parking",
                            "slug": "parking",
                            "icon": "car"
                        },
                        {
                            "id": 2,
                            "uuid": "amen-uuid-2",
                            "name": "Wi-Fi",
                            "slug": "wi-fi",
                            "icon": null
                        }
                    ]
                }
            }
        """.trimIndent()

        private val NULLABLE_FIELDS_BODY = """
            {
                "listing": {
                    "id": 2,
                    "uuid": "019de8d4-0000-0000-0000-000000000000",
                    "title": "Basic Room",
                    "type": "studio",
                    "type_label": "Studio",
                    "price_monthly": 5000,
                    "beds": 1,
                    "baths": 1,
                    "sqm": 18,
                    "barangay": "carmen",
                    "barangay_label": "Carmen",
                    "latitude": null,
                    "longitude": null,
                    "description": null,
                    "verified_at": null,
                    "listed_at": null,
                    "images": [],
                    "amenities": []
                }
            }
        """.trimIndent()
    }
}
