package ph.rentconnect.app.feature.detail.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.detail.data.ListingAmenity
import ph.rentconnect.app.feature.detail.data.ListingDetail
import ph.rentconnect.app.feature.detail.data.ListingDetailRepository
import ph.rentconnect.app.feature.detail.data.ListingImage

@OptIn(ExperimentalCoroutinesApi::class)
class ListingDetailViewModelTest {

    private val repository = mockk<ListingDetailRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it emits Loading then Success when repository returns listing`() = runTest {
        val listing = listingDetail()
        val deferred = CompletableDeferred<Result<ListingDetail>>()
        coEvery { repository.getListing(any()) } coAnswers { deferred.await() }

        val viewModel = ListingDetailViewModel("abc-123", repository)

        viewModel.uiState.test {
            assertEquals(ListingDetailUiState.Loading, awaitItem())
            deferred.complete(Result.Success(listing))
            val success = awaitItem()
            assertTrue(success is ListingDetailUiState.Success)
            assertEquals("Larkin Harbor Suites", (success as ListingDetailUiState.Success).listing.title)
        }
    }

    @Test
    fun `it emits Loading then NotFound on 404`() = runTest {
        val deferred = CompletableDeferred<Result<ListingDetail>>()
        coEvery { repository.getListing(any()) } coAnswers { deferred.await() }

        val viewModel = ListingDetailViewModel("bad-uuid", repository)

        viewModel.uiState.test {
            assertEquals(ListingDetailUiState.Loading, awaitItem())
            deferred.complete(Result.Failure(ApiError.NotFound))
            val state = awaitItem()
            assertTrue(state is ListingDetailUiState.NotFound)
        }
    }

    @Test
    fun `it emits Loading then Error on network failure`() = runTest {
        val deferred = CompletableDeferred<Result<ListingDetail>>()
        coEvery { repository.getListing(any()) } coAnswers { deferred.await() }

        val viewModel = ListingDetailViewModel("abc-123", repository)

        viewModel.uiState.test {
            assertEquals(ListingDetailUiState.Loading, awaitItem())
            deferred.complete(Result.Failure(ApiError.NetworkError(Exception("offline"))))
            val state = awaitItem()
            assertTrue(state is ListingDetailUiState.Error)
        }
    }

    @Test
    fun `it reloads data on retry`() = runTest {
        coEvery { repository.getListing(any()) } returns
            Result.Failure(ApiError.NetworkError(Exception("offline"))) andThen
            Result.Success(listingDetail())

        val viewModel = ListingDetailViewModel("abc-123", repository)

        viewModel.uiState.test {
            val error = awaitItem()
            assertTrue(error is ListingDetailUiState.Error)

            viewModel.retry()
            var next = awaitItem()
            if (next is ListingDetailUiState.Loading) {
                next = awaitItem()
            }
            assertTrue(next is ListingDetailUiState.Success)
        }
    }

    @Test
    fun `it passes uuid to repository`() = runTest {
        val uuid = "019de8d4-9ab7-73b4-90ca-b439185d4e64"
        coEvery { repository.getListing(uuid) } returns Result.Success(listingDetail(uuid = uuid))

        val viewModel = ListingDetailViewModel(uuid, repository)

        viewModel.uiState.test {
            val success = awaitItem()
            assertTrue(success is ListingDetailUiState.Success)
            assertEquals(uuid, (success as ListingDetailUiState.Success).listing.uuid)
        }
    }

    private fun listingDetail(
        id: Int = 1,
        uuid: String = "abc-123",
        title: String = "Larkin Harbor Suites",
        type: String = "apartment",
        typeLabel: String = "Apartment",
        priceMonthly: Int = 64000,
        beds: Int = 1,
        baths: Int = 2,
        sqm: Int = 29,
        barangay: String = "uptown",
        barangayLabel: String = "Uptown",
        latitude: Double? = 8.15,
        longitude: Double? = 124.96,
        description: String? = "Modern minimalist finish.",
        verifiedAt: String? = "2026-05-01T00:00:00+08:00",
        listedAt: String? = "2026-04-20T00:00:00+08:00",
        images: List<ListingImage> = listOf(
            ListingImage(10, "img-1", "https://cdn.example.com/img1.jpg", 1),
        ),
        amenities: List<ListingAmenity> = listOf(
            ListingAmenity(1, "amen-1", "Parking", "parking", "car"),
        ),
    ) = ListingDetail(
        id, uuid, title, type, typeLabel, priceMonthly, beds, baths, sqm,
        barangay, barangayLabel, latitude, longitude, description, verifiedAt, listedAt,
        images, amenities,
    )
}
