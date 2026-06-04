package ph.rentconnect.app.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Response

class MinVersionInterceptor : Interceptor {

    private val _minVersion = MutableStateFlow<String?>(null)
    val minVersion: StateFlow<String?> = _minVersion.asStateFlow()

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        response.header("X-Min-Android-Consumer-Version")?.let { _minVersion.value = it }
        return response
    }
}
