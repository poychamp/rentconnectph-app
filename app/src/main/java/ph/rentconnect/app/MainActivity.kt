package ph.rentconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.core.persistence.ThemePreferences
import ph.rentconnect.app.feature.home.data.HomeApi
import ph.rentconnect.app.feature.home.data.HomeRepository
import ph.rentconnect.app.feature.home.ui.HomeScreen
import ph.rentconnect.app.feature.home.ui.HomeViewModel
import ph.rentconnect.app.feature.home.ui.HomeViewModelFactory
import ph.rentconnect.app.ui.theme.RentConnectAppTheme
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MainActivity : ComponentActivity() {

    private val themePreferences by lazy { ThemePreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(provideHomeRepository()),
        )[HomeViewModel::class.java]

        setContent {
            val themeMode by themePreferences.themeMode
                .collectAsStateWithLifecycle(ThemeMode.System)

            val darkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> isSystemInDarkTheme()
            }

            RentConnectAppTheme(darkTheme = darkTheme) {
                HomeScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onThemeToggle = themePreferences::setThemeMode,
                )
            }
        }
    }

    private fun provideHomeRepository(): HomeRepository {
        val json = Json { ignoreUnknownKeys = true }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                },
            )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(HomeApi::class.java)
        return HomeRepository(api)
    }
}
