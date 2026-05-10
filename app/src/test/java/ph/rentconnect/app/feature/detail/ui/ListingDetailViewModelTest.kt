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
import ph.rentconnect.app.feature.detail.data.InquiryRepository
import ph.rentconnect.app.feature.detail.data.InquiryResult
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

    // --- Inquiry dialog tests ---

    @Test
    fun `it opens inquiry dialog`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            assertEquals(InquiryDialogState.Hidden, awaitItem())
            viewModel.openInquiryDialog()
            val state = awaitItem()
            assertTrue(state is InquiryDialogState.Visible)
            assertEquals("", (state as InquiryDialogState.Visible).name)
            assertEquals("", state.phone)
        }
    }

    @Test
    fun `it closes inquiry dialog`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            assertEquals(InquiryDialogState.Hidden, awaitItem())
            viewModel.openInquiryDialog()
            assertTrue(awaitItem() is InquiryDialogState.Visible)
            viewModel.closeInquiryDialog()
            assertEquals(InquiryDialogState.Hidden, awaitItem())
        }
    }

    @Test
    fun `it submits inquiry and shows success`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()
        coEvery { inquiryRepository.submitInquiry(any(), any(), any()) } returns InquiryResult.Success

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            skipItems(1) // Hidden
            viewModel.openInquiryDialog()
            skipItems(1) // Visible
            viewModel.updateInquiryName("Maria Cruz")
            skipItems(1)
            viewModel.updateInquiryPhone("09171234567")
            skipItems(1)
            viewModel.submitInquiry()
            // Submitting state
            val submitting = awaitItem()
            assertTrue(submitting is InquiryDialogState.Visible)
            assertTrue((submitting as InquiryDialogState.Visible).isSubmitting)
            // Success state
            val success = awaitItem()
            assertTrue(success is InquiryDialogState.Success)
        }
    }

    @Test
    fun `it shows validation errors from server on 422`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()
        val errors = mapOf("name" to listOf("Name is required."), "phone" to listOf("Invalid PH mobile number."))
        coEvery { inquiryRepository.submitInquiry(any(), any(), any()) } returns InquiryResult.ValidationError(errors)

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            skipItems(1) // Hidden
            viewModel.openInquiryDialog()
            skipItems(1) // Visible
            viewModel.updateInquiryName("Maria Cruz")
            skipItems(1)
            viewModel.updateInquiryPhone("09171234567")
            skipItems(1)
            viewModel.submitInquiry()
            skipItems(1) // Submitting
            val state = awaitItem()
            assertTrue(state is InquiryDialogState.Visible)
            val visible = state as InquiryDialogState.Visible
            assertEquals(listOf("Name is required."), visible.fieldErrors["name"])
            assertEquals(listOf("Invalid PH mobile number."), visible.fieldErrors["phone"])
            assertTrue(!visible.isSubmitting)
        }
    }

    @Test
    fun `it shows throttle error on 429`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()
        coEvery { inquiryRepository.submitInquiry(any(), any(), any()) } returns InquiryResult.Throttled

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            skipItems(1) // Hidden
            viewModel.openInquiryDialog()
            skipItems(1) // Visible
            viewModel.updateInquiryName("Maria Cruz")
            skipItems(1)
            viewModel.updateInquiryPhone("09171234567")
            skipItems(1)
            viewModel.submitInquiry()
            skipItems(1) // Submitting
            val state = awaitItem()
            assertTrue(state is InquiryDialogState.Visible)
            val visible = state as InquiryDialogState.Visible
            assertEquals("Too many inquiries. Please try again later.", visible.submitError)
            assertTrue(!visible.isSubmitting)
        }
    }

    @Test
    fun `it shows network error on submit failure`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()
        coEvery { inquiryRepository.submitInquiry(any(), any(), any()) } returns InquiryResult.NetworkError

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            skipItems(1) // Hidden
            viewModel.openInquiryDialog()
            skipItems(1) // Visible
            viewModel.updateInquiryName("Maria Cruz")
            skipItems(1)
            viewModel.updateInquiryPhone("09171234567")
            skipItems(1)
            viewModel.submitInquiry()
            skipItems(1) // Submitting
            val state = awaitItem()
            assertTrue(state is InquiryDialogState.Visible)
            val visible = state as InquiryDialogState.Visible
            assertEquals("Connection error. Please check your internet and try again.", visible.submitError)
            assertTrue(!visible.isSubmitting)
        }
    }

    @Test
    fun `it dismisses success state back to hidden`() = runTest {
        coEvery { repository.getListing(any()) } returns Result.Success(listingDetail())
        val inquiryRepository = mockk<InquiryRepository>()
        coEvery { inquiryRepository.submitInquiry(any(), any(), any()) } returns InquiryResult.Success

        val viewModel = ListingDetailViewModel("abc-123", repository, inquiryRepository)

        viewModel.inquiryState.test {
            skipItems(1) // Hidden
            viewModel.openInquiryDialog()
            skipItems(1) // Visible
            viewModel.updateInquiryName("Maria Cruz")
            skipItems(1)
            viewModel.updateInquiryPhone("09171234567")
            skipItems(1)
            viewModel.submitInquiry()
            skipItems(1) // Submitting
            assertTrue(awaitItem() is InquiryDialogState.Success)
            viewModel.closeInquiryDialog()
            assertEquals(InquiryDialogState.Hidden, awaitItem())
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
