package ph.rentconnect.app.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.rentconnect.app.R
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.feature.home.ui.components.HeroSection
import androidx.compose.foundation.isSystemInDarkTheme
import ph.rentconnect.app.feature.home.ui.components.ListingCard
import ph.rentconnect.app.ui.theme.Orange500

private const val DEBOUNCE_MS = 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onListingClick: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSearchWithFilters: (
        q: String?,
        area: String?,
        type: String?,
        budgetMin: Int?,
        budgetMax: Int?,
    ) -> Unit = { _, _, _, _, _ -> },
    onNavigateToAbout: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isDarkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }

    // Local filter state for debounce
    var searchQuery by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf<String?>(null) }
    var budgetMin by remember { mutableStateOf<Int?>(null) }
    var budgetMax by remember { mutableStateOf<Int?>(null) }
    var selectedTypes by remember { mutableStateOf<List<String>>(emptyList()) }

    // Debounce timers
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var typeJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleSearchNavigation() {
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(DEBOUNCE_MS)
            onNavigateToSearchWithFilters(
                searchQuery.ifBlank { null },
                selectedArea,
                selectedTypes.takeIf { it.isNotEmpty() }?.joinToString(","),
                budgetMin,
                budgetMax,
            )
        }
    }

    fun scheduleTypeNavigation() {
        typeJob?.cancel()
        typeJob = scope.launch {
            delay(DEBOUNCE_MS)
            onNavigateToSearchWithFilters(
                searchQuery.ifBlank { null },
                selectedArea,
                selectedTypes.takeIf { it.isNotEmpty() }?.joinToString(","),
                budgetMin,
                budgetMax,
            )
        }
    }

    fun submitNow() {
        searchJob?.cancel()
        typeJob?.cancel()
        onNavigateToSearchWithFilters(
            searchQuery.ifBlank { null },
            selectedArea,
            selectedTypes.takeIf { it.isNotEmpty() }?.joinToString(","),
            budgetMin,
            budgetMax,
        )
    }

    Scaffold(
        topBar = { LogoTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle) },
        bottomBar = { AppBottomBar(onNavigateToSearch = onNavigateToSearch, onNavigateToAbout = onNavigateToAbout, onNavigateToContact = onNavigateToContact) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> LoadingContent(PaddingValues())
                is HomeUiState.Error -> ErrorContent(PaddingValues(), onRetry = viewModel::refresh)
                is HomeUiState.Success -> SuccessContent(
                    padding = PaddingValues(),
                    state = state,
                    isDarkTheme = isDarkTheme,
                    onListingClick = onListingClick,
                    searchQuery = searchQuery,
                    selectedArea = selectedArea,
                    budgetMin = budgetMin,
                    budgetMax = budgetMax,
                    selectedTypes = selectedTypes,
                    onSearchQueryChange = { query ->
                        searchQuery = query
                        scheduleSearchNavigation()
                    },
                    onAreaChange = { area ->
                        selectedArea = area
                        scheduleSearchNavigation()
                    },
                    onBudgetChange = { min, max ->
                        budgetMin = min
                        budgetMax = max
                        scheduleSearchNavigation()
                    },
                    onTypeToggle = { type ->
                        selectedTypes = if (type in selectedTypes) {
                            selectedTypes - type
                        } else {
                            selectedTypes + type
                        }
                        scheduleTypeNavigation()
                    },
                    onClearTypes = {
                        selectedTypes = emptyList()
                        scheduleTypeNavigation()
                    },
                    onSubmit = ::submitNow,
                )
            }
        }
    }
}

@Composable
private fun LogoTopBar(
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_logo),
                contentDescription = "RentConnectPH",
                modifier = Modifier.size(36.dp),
                tint = Color.Unspecified,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "RentConnect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "PH",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Orange500,
            )
        }
        IconButton(onClick = {
            val next = when (themeMode) {
                ThemeMode.System -> ThemeMode.Dark
                ThemeMode.Dark -> ThemeMode.Light
                ThemeMode.Light -> ThemeMode.System
            }
            scope.launch { onThemeToggle(next) }
        }) {
            Icon(
                imageVector = when (themeMode) {
                    ThemeMode.System -> Icons.Filled.LightMode
                    ThemeMode.Light -> Icons.Filled.LightMode
                    ThemeMode.Dark -> Icons.Filled.DarkMode
                },
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AppBottomBar(onNavigateToSearch: () -> Unit, onNavigateToAbout: () -> Unit, onNavigateToContact: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Orange500,
                selectedTextColor = Orange500,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToSearch,
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Orange500,
                selectedTextColor = Orange500,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToAbout,
            icon = { Icon(Icons.Filled.Info, contentDescription = "About") },
            label = { Text("About", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Orange500,
                selectedTextColor = Orange500,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent,
            ),
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToContact,
            icon = { Icon(Icons.Filled.Email, contentDescription = "Contact") },
            label = { Text("Contact", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Orange500,
                selectedTextColor = Orange500,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun LoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Orange500)
    }
}

@Composable
private fun ErrorContent(padding: PaddingValues, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun SuccessContent(
    padding: PaddingValues,
    state: HomeUiState.Success,
    isDarkTheme: Boolean,
    onListingClick: (String) -> Unit,
    searchQuery: String,
    selectedArea: String?,
    budgetMin: Int?,
    budgetMax: Int?,
    selectedTypes: List<String>,
    onSearchQueryChange: (String) -> Unit,
    onAreaChange: (String?) -> Unit,
    onBudgetChange: (Int?, Int?) -> Unit,
    onTypeToggle: (String) -> Unit,
    onClearTypes: () -> Unit,
    onSubmit: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val containerModifier = if (isTablet) {
            Modifier
                .widthIn(max = 900.dp)
                .align(Alignment.TopCenter)
        } else {
            Modifier
        }

        LazyColumn(
            contentPadding = padding,
            modifier = containerModifier.fillMaxSize(),
        ) {
            // Hero
            item {
                HeroSection(
                    barangays = state.catalogs.barangays.orEmpty(),
                    searchQuery = searchQuery,
                    selectedArea = selectedArea,
                    budgetMin = budgetMin,
                    budgetMax = budgetMax,
                    onSearchQueryChange = onSearchQueryChange,
                    onAreaChange = onAreaChange,
                    onBudgetChange = onBudgetChange,
                    onSubmit = onSubmit,
                    isDarkTheme = isDarkTheme,
                )
            }

            // Property type chips
            item {
                PropertyTypeChips(
                    types = state.catalogs.listingTypes.orEmpty(),
                    selectedTypes = selectedTypes,
                    onTypeToggle = onTypeToggle,
                    onClearTypes = onClearTypes,
                )
            }

            // Featured Listings
            item {
                SectionHeader(
                    title = "Featured Listings",
                    subtitle = "Hand-picked by RentConnectPH",
                )
            }
            if (isTablet) {
                items(state.featured.chunked(2), key = { "featured_row_${it.first().uuid}" }) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { listing ->
                            ListingCard(
                                listing,
                                onClick = { onListingClick(listing.uuid) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(state.featured, key = { "featured_${it.uuid}" }) { listing ->
                    ListingCard(listing, onClick = { onListingClick(listing.uuid) })
                }
            }

            // Divider
            item {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }

            // Recently Verified
            item {
                SectionHeader(
                    title = "Recently Verified",
                    subtitle = "Fresh listings, all ground-checked",
                    actionLabel = "View all →",
                )
            }
            if (isTablet) {
                items(state.recently.chunked(2), key = { "recently_row_${it.first().uuid}" }) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { listing ->
                            ListingCard(
                                listing,
                                onClick = { onListingClick(listing.uuid) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            } else {
                items(state.recently, key = { "recently_${it.uuid}" }) { listing ->
                    ListingCard(listing, onClick = { onListingClick(listing.uuid) })
                }
            }

            // Bottom spacer
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PropertyTypeChips(
    types: List<CatalogItem>,
    selectedTypes: List<String>,
    onTypeToggle: (String) -> Unit,
    onClearTypes: () -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // "All" chip
        item {
            FilterChip(
                selected = selectedTypes.isEmpty(),
                onClick = onClearTypes,
                label = {
                    Text(
                        text = "All",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Orange500,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Orange500,
                    enabled = true,
                    selected = selectedTypes.isEmpty(),
                ),
            )
        }
        items(types) { type ->
            FilterChip(
                selected = type.value in selectedTypes,
                onClick = { onTypeToggle(type.value ?: "") },
                label = {
                    Text(
                        text = type.label ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Orange500,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = Orange500,
                    enabled = true,
                    selected = type.value in selectedTypes,
                ),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
            )
            if (actionLabel != null) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Orange500,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
