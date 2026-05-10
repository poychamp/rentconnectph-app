package ph.rentconnect.app.feature.search.ui

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ph.rentconnect.app.core.network.ApiError
import ph.rentconnect.app.core.network.Result
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.feature.home.data.ListingCard
import ph.rentconnect.app.feature.search.data.FiltersEcho
import ph.rentconnect.app.feature.search.data.PaginationMeta
import ph.rentconnect.app.feature.search.data.SearchCatalogs
import ph.rentconnect.app.feature.search.data.SearchRepository
import ph.rentconnect.app.feature.search.data.SearchResponse

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<SearchRepository>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it loads initial browse results on init`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), any()) } returns
            Result.Success(page1Response())

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.items.size)
        assertEquals("Listing A", state.items[0].title)
        assertEquals("Listing B", state.items[1].title)
        assertEquals(1, state.currentPage)
        assertEquals(3, state.lastPage)
        assertEquals(50, state.total)
    }

    @Test
    fun `it appends items on load next page`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(1)) } returns
            Result.Success(page1Response())
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(2)) } returns
            Result.Success(page2Response())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(4, state.items.size)
        assertEquals("Listing A", state.items[0].title)
        assertEquals("Listing B", state.items[1].title)
        assertEquals("Listing C", state.items[2].title)
        assertEquals("Listing D", state.items[3].title)
        assertEquals(2, state.currentPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `it guards against loading past last page`() = runTest {
        val lastPageResponse = page1Response().copy(
            meta = PaginationMeta(currentPage = 1, lastPage = 1, perPage = 24, total = 2),
        )
        coEvery { repository.search(any(), any(), any(), any(), any(), any()) } returns
            Result.Success(lastPageResponse)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        coVerify(exactly = 1) { repository.search(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `it resets and reloads on search query change`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(1)) } returns
            Result.Success(page1Response())

        val vm = createViewModel()
        advanceUntilIdle()

        val searchResult = SearchResponse(
            data = listOf(listing("3", "WiFi Studio")),
            meta = PaginationMeta(currentPage = 1, lastPage = 1, perPage = 24, total = 1),
            filters = FiltersEcho(q = "wifi"),
            catalogs = defaultCatalogs(),
        )
        coEvery { repository.search(eq("wifi"), any(), any(), any(), any(), eq(1)) } returns
            Result.Success(searchResult)

        vm.onSearchQueryChange("wifi")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("WiFi Studio", state.items[0].title)
        assertEquals(1, state.currentPage)
        assertEquals("wifi", state.searchQuery)
    }

    @Test
    fun `it resets and reloads on area filter change`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), any()) } returns
            Result.Success(page1Response())

        val vm = createViewModel()
        advanceUntilIdle()

        val areaResult = SearchResponse(
            data = listOf(listing("4", "Carmen Place")),
            meta = PaginationMeta(currentPage = 1, lastPage = 1, perPage = 24, total = 1),
            filters = FiltersEcho(area = "carmen"),
            catalogs = defaultCatalogs(),
        )
        coEvery { repository.search(any(), eq("carmen"), any(), any(), any(), eq(1)) } returns
            Result.Success(areaResult)

        vm.onAreaChange("carmen")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Carmen Place", state.items[0].title)
        assertEquals("carmen", state.selectedArea)
    }

    @Test
    fun `it shows error state on initial load failure`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), any()) } returns
            Result.Failure(ApiError.NetworkError(java.io.IOException("timeout")))

        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.items.isEmpty())
        assertTrue(state.error is ApiError.NetworkError)
    }

    @Test
    fun `it keeps items on load more failure`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(1)) } returns
            Result.Success(page1Response())
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(2)) } returns
            Result.Failure(ApiError.ServerError(500))

        val vm = createViewModel()
        advanceUntilIdle()

        vm.loadNextPage()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.items.size)
        assertFalse(state.isLoadingMore)
        assertTrue(state.error is ApiError.ServerError)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `it resets and reloads on refresh`() = runTest {
        coEvery { repository.search(any(), any(), any(), any(), any(), any()) } returns
            Result.Success(page1Response())

        val vm = createViewModel()
        advanceUntilIdle()

        // Simulate having loaded page 2
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(2)) } returns
            Result.Success(page2Response())
        vm.loadNextPage()
        advanceUntilIdle()
        assertEquals(4, vm.uiState.value.items.size)

        // Refresh should reset to page 1
        val freshResponse = page1Response().copy(
            data = listOf(listing("5", "Fresh Listing")),
            meta = PaginationMeta(currentPage = 1, lastPage = 2, perPage = 24, total = 30),
        )
        coEvery { repository.search(any(), any(), any(), any(), any(), eq(1)) } returns
            Result.Success(freshResponse)

        vm.refresh()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("Fresh Listing", state.items[0].title)
        assertEquals(1, state.currentPage)
    }

    @Test
    fun `it uses initial filter params for first load`() = runTest {
        val filteredResult = SearchResponse(
            data = listOf(listing("5", "Filtered Result")),
            meta = PaginationMeta(currentPage = 1, lastPage = 1, perPage = 24, total = 1),
            filters = FiltersEcho(q = "wifi", area = "carmen"),
            catalogs = defaultCatalogs(),
        )
        coEvery { repository.search(eq("wifi"), eq("carmen"), eq("studio"), eq(5000), eq(20000), eq(1)) } returns
            Result.Success(filteredResult)

        val vm = SearchViewModel(
            repository = repository,
            initialQuery = "wifi",
            initialArea = "carmen",
            initialType = "studio",
            initialBudgetMin = 5000,
            initialBudgetMax = 20000,
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("wifi", state.searchQuery)
        assertEquals("carmen", state.selectedArea)
        assertEquals(listOf("studio"), state.selectedTypes)
        assertEquals(5000, state.budgetMin)
        assertEquals(20000, state.budgetMax)
        assertEquals(1, state.items.size)
        assertEquals("Filtered Result", state.items[0].title)
    }

    private fun createViewModel() = SearchViewModel(repository)

    private fun listing(uuid: String, title: String) = ListingCard(
        id = uuid.hashCode(),
        uuid = uuid,
        title = title,
        type = "studio",
        typeLabel = "Studio",
        priceMonthly = 15000,
        beds = 1,
        baths = 1,
        sqm = 30,
        barangay = "carmen",
        barangayLabel = "Carmen",
        image = "https://example.com/$uuid.jpg",
        imageCount = 3,
        section = "verified",
    )

    private fun defaultCatalogs() = SearchCatalogs(
        listingTypes = listOf(CatalogItem("apartment", "Apartment"), CatalogItem("studio", "Studio")),
        barangays = listOf(CatalogItem("carmen", "Carmen"), CatalogItem("uptown", "Uptown")),
    )

    private fun page1Response() = SearchResponse(
        data = listOf(listing("1", "Listing A"), listing("2", "Listing B")),
        meta = PaginationMeta(currentPage = 1, lastPage = 3, perPage = 24, total = 50),
        filters = FiltersEcho(),
        catalogs = defaultCatalogs(),
    )

    private fun page2Response() = SearchResponse(
        data = listOf(listing("3", "Listing C"), listing("4", "Listing D")),
        meta = PaginationMeta(currentPage = 2, lastPage = 3, perPage = 24, total = 50),
        filters = FiltersEcho(),
        catalogs = defaultCatalogs(),
    )
}
