package ph.rentconnect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.core.persistence.ThemePreferences
import ph.rentconnect.app.feature.detail.data.InquiryApi
import ph.rentconnect.app.feature.detail.data.InquiryRepository
import ph.rentconnect.app.feature.detail.data.ListingDetailApi
import ph.rentconnect.app.feature.detail.data.ListingDetailRepository
import ph.rentconnect.app.feature.detail.ui.InquirySuccessScreen
import ph.rentconnect.app.feature.detail.ui.ListingDetailScreen
import ph.rentconnect.app.feature.detail.ui.ListingDetailViewModel
import ph.rentconnect.app.feature.detail.ui.ListingDetailViewModelFactory
import ph.rentconnect.app.feature.home.data.HomeApi
import ph.rentconnect.app.feature.home.data.HomeRepository
import ph.rentconnect.app.feature.home.ui.HomeScreen
import ph.rentconnect.app.feature.home.ui.HomeViewModel
import ph.rentconnect.app.feature.home.ui.HomeViewModelFactory
import ph.rentconnect.app.feature.search.data.SearchApi
import ph.rentconnect.app.feature.search.data.SearchRepository
import ph.rentconnect.app.feature.search.ui.SearchScreen
import ph.rentconnect.app.feature.search.ui.SearchViewModel
import ph.rentconnect.app.feature.search.ui.SearchViewModelFactory
import ph.rentconnect.app.ui.theme.RentConnectAppTheme
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Serializable
data object HomeRoute

@Serializable
data class SearchRoute(
    val q: String? = null,
    val area: String? = null,
    val type: String? = null,
    val budgetMin: Int? = null,
    val budgetMax: Int? = null,
)

@Serializable
data class DetailRoute(val uuid: String)

@Serializable
data class InquirySuccessRoute(val listingName: String)

class MainActivity : ComponentActivity() {

    private val themePreferences by lazy { ThemePreferences(applicationContext) }
    private val retrofit by lazy { provideRetrofit() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val homeViewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(HomeRepository(retrofit.create(HomeApi::class.java))),
        )[HomeViewModel::class.java]

        val json = Json { ignoreUnknownKeys = true }
        val detailRepository = ListingDetailRepository(retrofit.create(ListingDetailApi::class.java))
        val inquiryRepository = InquiryRepository(retrofit.create(InquiryApi::class.java), json)
        val searchRepository = SearchRepository(retrofit.create(SearchApi::class.java))

        setContent {
            val themeMode by themePreferences.themeMode
                .collectAsStateWithLifecycle(ThemeMode.System)

            val darkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System -> isSystemInDarkTheme()
            }

            RentConnectAppTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = HomeRoute) {
                    composable<HomeRoute> {
                        HomeScreen(
                            viewModel = homeViewModel,
                            themeMode = themeMode,
                            onThemeToggle = themePreferences::setThemeMode,
                            onListingClick = { uuid ->
                                navController.navigate(DetailRoute(uuid))
                            },
                            onNavigateToSearch = {
                                navController.navigate(SearchRoute()) {
                                    popUpTo(HomeRoute) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onNavigateToSearchWithFilters = { q, area, type, budgetMin, budgetMax ->
                                navController.navigate(
                                    SearchRoute(
                                        q = q,
                                        area = area,
                                        type = type,
                                        budgetMin = budgetMin,
                                        budgetMax = budgetMax,
                                    ),
                                ) {
                                    popUpTo(HomeRoute) { saveState = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable<SearchRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<SearchRoute>()
                        val searchViewModel = ViewModelProvider(
                            backStackEntry,
                            SearchViewModelFactory(
                                repository = searchRepository,
                                initialQuery = route.q,
                                initialArea = route.area,
                                initialType = route.type,
                                initialBudgetMin = route.budgetMin,
                                initialBudgetMax = route.budgetMax,
                            ),
                        )[SearchViewModel::class.java]

                        SearchScreen(
                            viewModel = searchViewModel,
                            themeMode = themeMode,
                            onThemeToggle = themePreferences::setThemeMode,
                            onListingClick = { uuid ->
                                navController.navigate(DetailRoute(uuid))
                            },
                            onNavigateToHome = {
                                navController.popBackStack(HomeRoute, inclusive = false)
                            },
                        )
                    }
                    composable<DetailRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<DetailRoute>()
                        val detailViewModel = ViewModelProvider(
                            backStackEntry,
                            ListingDetailViewModelFactory(route.uuid, detailRepository, inquiryRepository),
                        )[ListingDetailViewModel::class.java]

                        ListingDetailScreen(
                            viewModel = detailViewModel,
                            themeMode = themeMode,
                            onThemeToggle = themePreferences::setThemeMode,
                            onBack = { navController.popBackStack() },
                            onInquirySuccess = { listingName ->
                                navController.navigate(InquirySuccessRoute(listingName)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                    composable<InquirySuccessRoute> { backStackEntry ->
                        val route = backStackEntry.toRoute<InquirySuccessRoute>()
                        InquirySuccessScreen(
                            listingName = route.listingName,
                            themeMode = themeMode,
                            onThemeToggle = themePreferences::setThemeMode,
                            onBack = { navController.popBackStack() },
                            onBrowseMore = {
                                navController.navigate(SearchRoute()) {
                                    popUpTo(HomeRoute) { saveState = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    private fun provideRetrofit(): Retrofit {
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

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
