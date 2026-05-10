package ph.rentconnect.app.feature.home.ui

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
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.feature.home.data.Catalogs
import ph.rentconnect.app.feature.home.data.HomeRepository
import ph.rentconnect.app.feature.home.data.HomeResponse
import ph.rentconnect.app.feature.home.data.ListingCard

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val repository = mockk<HomeRepository>()
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
    fun `it emits Loading then Success when repository returns data`() = runTest {
        val response = HomeResponse(
            featured = listOf(listingCard()),
            recently = listOf(listingCard(uuid = "def-456", title = "Another Place")),
            catalogs = catalogs(),
        )
        val deferred = CompletableDeferred<Result<HomeResponse>>()
        coEvery { repository.getHome() } coAnswers { deferred.await() }

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            deferred.complete(Result.Success(response))
            val success = awaitItem()
            assertTrue(success is HomeUiState.Success)
            assertEquals(1, (success as HomeUiState.Success).featured.size)
            assertEquals(1, success.recently.size)
        }
    }

    @Test
    fun `it emits Loading then Error when repository returns failure`() = runTest {
        val deferred = CompletableDeferred<Result<HomeResponse>>()
        coEvery { repository.getHome() } coAnswers { deferred.await() }

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            assertEquals(HomeUiState.Loading, awaitItem())
            deferred.complete(Result.Failure(ApiError.NetworkError(Exception("no internet"))))
            val error = awaitItem()
            assertTrue(error is HomeUiState.Error)
            assertTrue((error as HomeUiState.Error).error is ApiError.NetworkError)
        }
    }

    @Test
    fun `it emits Success with empty lists when no listings exist`() = runTest {
        val response = HomeResponse(
            featured = emptyList(),
            recently = emptyList(),
            catalogs = catalogs(),
        )
        coEvery { repository.getHome() } returns Result.Success(response)

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is HomeUiState.Success)
            assertTrue((state as HomeUiState.Success).featured.isEmpty())
            assertTrue(state.recently.isEmpty())
            assertEquals(1, state.catalogs.listingTypes.size)
        }
    }

    @Test
    fun `it reloads data on refresh`() = runTest {
        val first = HomeResponse(
            featured = listOf(listingCard()),
            recently = emptyList(),
            catalogs = catalogs(),
        )
        val second = HomeResponse(
            featured = listOf(listingCard(), listingCard(uuid = "xyz-789")),
            recently = emptyList(),
            catalogs = catalogs(),
        )
        coEvery { repository.getHome() } returns Result.Success(first) andThen Result.Success(second)

        val viewModel = HomeViewModel(repository)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial is HomeUiState.Success)
            assertEquals(1, (initial as HomeUiState.Success).featured.size)

            viewModel.refresh()
            // Skip Loading if emitted (StateFlow may or may not conflate it)
            var refreshed = awaitItem()
            if (refreshed is HomeUiState.Loading) {
                refreshed = awaitItem()
            }
            assertTrue(refreshed is HomeUiState.Success)
            assertEquals(2, (refreshed as HomeUiState.Success).featured.size)
        }
    }

    private fun listingCard(
        id: Int = 1,
        uuid: String = "abc-123",
        title: String = "Larkin Harbor Suites",
        type: String = "apartment",
        typeLabel: String = "Apartment",
        priceMonthly: Int? = 64000,
        beds: Int = 1,
        baths: Int = 2,
        sqm: Int = 29,
        barangay: String = "uptown",
        barangayLabel: String = "Uptown",
        image: String? = "https://example.com/image1.jpg",
        imageCount: Int = 4,
        section: String = "verified",
    ) = ListingCard(id, uuid, title, type, typeLabel, priceMonthly, beds, baths, sqm, barangay, barangayLabel, image, imageCount, section)

    private fun catalogs() = Catalogs(
        listingTypes = listOf(CatalogItem("apartment", "Apartment")),
        barangays = listOf(CatalogItem("uptown", "Uptown")),
    )
}
