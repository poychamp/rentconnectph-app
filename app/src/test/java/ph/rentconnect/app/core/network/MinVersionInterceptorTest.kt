package ph.rentconnect.app.core.network

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MinVersionInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var interceptor: MinVersionInterceptor
    private lateinit var client: OkHttpClient

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        interceptor = MinVersionInterceptor()
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it reads min version from response header`() {
        server.enqueue(MockResponse().addHeader("X-Min-Android-Consumer-Version", "1.2.0").setBody("{}"))

        client.newCall(okhttp3.Request.Builder().url(server.url("/")).build()).execute()

        assertEquals("1.2.0", interceptor.minVersion.value)
    }

    @Test
    fun `it leaves min version null when header is missing`() {
        server.enqueue(MockResponse().setBody("{}"))

        client.newCall(okhttp3.Request.Builder().url(server.url("/")).build()).execute()

        assertNull(interceptor.minVersion.value)
    }

    @Test
    fun `it updates min version on subsequent responses`() {
        server.enqueue(MockResponse().addHeader("X-Min-Android-Consumer-Version", "1.0.0").setBody("{}"))
        server.enqueue(MockResponse().addHeader("X-Min-Android-Consumer-Version", "1.3.0").setBody("{}"))

        client.newCall(okhttp3.Request.Builder().url(server.url("/")).build()).execute()
        assertEquals("1.0.0", interceptor.minVersion.value)

        client.newCall(okhttp3.Request.Builder().url(server.url("/")).build()).execute()
        assertEquals("1.3.0", interceptor.minVersion.value)
    }
}
