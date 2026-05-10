package ph.rentconnect.app.feature.contact.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ph.rentconnect.app.feature.contact.data.ContactRepository
import ph.rentconnect.app.feature.contact.data.ContactResult

@OptIn(ExperimentalCoroutinesApi::class)
class ContactViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ContactRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `it starts with empty Form state`() = runTest {
        val vm = ContactViewModel(repository)

        vm.uiState.test {
            val state = awaitItem()
            assertTrue(state is ContactUiState.Form)
            val form = state as ContactUiState.Form
            assertEquals("", form.name)
            assertEquals("", form.email)
            assertEquals("", form.message)
            assertEquals(emptyMap<String, List<String>>(), form.fieldErrors)
            assertEquals(null, form.submitError)
            assertEquals(false, form.isSubmitting)
        }
    }

    @Test
    fun `it blocks submit and shows client errors when fields are empty`() = runTest {
        val vm = ContactViewModel(repository)

        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertTrue(state.fieldErrors.containsKey("name"))
            assertTrue(state.fieldErrors.containsKey("email"))
            assertTrue(state.fieldErrors.containsKey("message"))
            assertEquals(false, state.isSubmitting)
        }
    }

    @Test
    fun `it blocks submit when email format is invalid`() = runTest {
        val vm = ContactViewModel(repository)

        vm.updateName("Maria")
        vm.updateEmail("not-an-email")
        vm.updateMessage("Hello")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertTrue(state.fieldErrors.containsKey("email"))
            assertEquals(listOf("Please enter a valid email address."), state.fieldErrors["email"])
            assertTrue(!state.fieldErrors.containsKey("name"))
            assertTrue(!state.fieldErrors.containsKey("message"))
        }
    }

    @Test
    fun `it transitions to Success on successful submit`() = runTest {
        coEvery { repository.sendMessage(any(), any(), any()) } returns ContactResult.Success
        val vm = ContactViewModel(repository)

        vm.updateName("Maria Santos")
        vm.updateEmail("maria@example.com")
        vm.updateMessage("I want to list a property.")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            assertTrue(awaitItem() is ContactUiState.Success)
        }
    }

    @Test
    fun `it shows server validation errors on 422`() = runTest {
        coEvery { repository.sendMessage(any(), any(), any()) } returns ContactResult.ValidationError(
            mapOf("email" to listOf("Please enter a valid email address.")),
        )
        val vm = ContactViewModel(repository)

        vm.updateName("Maria")
        vm.updateEmail("maria@example.com")
        vm.updateMessage("Hello")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertEquals(listOf("Please enter a valid email address."), state.fieldErrors["email"])
            assertEquals(false, state.isSubmitting)
        }
    }

    @Test
    fun `it shows throttle error on 429`() = runTest {
        coEvery { repository.sendMessage(any(), any(), any()) } returns ContactResult.Throttled
        val vm = ContactViewModel(repository)

        vm.updateName("Maria")
        vm.updateEmail("maria@example.com")
        vm.updateMessage("Hello")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertEquals("Too many messages. Please try again later.", state.submitError)
            assertEquals(false, state.isSubmitting)
        }
    }

    @Test
    fun `it shows network error on connection failure`() = runTest {
        coEvery { repository.sendMessage(any(), any(), any()) } returns ContactResult.NetworkError
        val vm = ContactViewModel(repository)

        vm.updateName("Maria")
        vm.updateEmail("maria@example.com")
        vm.updateMessage("Hello")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertEquals("Connection error. Please check your internet and try again.", state.submitError)
            assertEquals(false, state.isSubmitting)
        }
    }

    @Test
    fun `it shows server error on 500`() = runTest {
        coEvery { repository.sendMessage(any(), any(), any()) } returns ContactResult.ServerError
        val vm = ContactViewModel(repository)

        vm.updateName("Maria")
        vm.updateEmail("maria@example.com")
        vm.updateMessage("Hello")
        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertEquals("Something went wrong. Please try again.", state.submitError)
            assertEquals(false, state.isSubmitting)
        }
    }

    @Test
    fun `it clears field error on focus`() = runTest {
        val vm = ContactViewModel(repository)

        vm.submitContact()
        testDispatcher.scheduler.advanceUntilIdle()
        vm.onFieldFocus("name")

        vm.uiState.test {
            val state = awaitItem() as ContactUiState.Form
            assertTrue(!state.fieldErrors.containsKey("name"))
            assertTrue(state.fieldErrors.containsKey("email"))
            assertTrue(state.fieldErrors.containsKey("message"))
        }
    }
}
