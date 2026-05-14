package ph.rentconnect.app.feature.search.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ph.rentconnect.app.R
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.feature.home.ui.components.AreaFilter
import ph.rentconnect.app.feature.home.ui.components.BudgetFilter
import ph.rentconnect.app.feature.home.ui.components.ListingCard
import ph.rentconnect.app.ui.theme.Orange500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onListingClick: (String) -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SearchTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle) },
        bottomBar = { SearchBottomBar(onNavigateToHome = onNavigateToHome, onNavigateToAbout = onNavigateToAbout, onNavigateToContact = onNavigateToContact) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                uiState.isLoading && uiState.items.isEmpty() -> LoadingContent()
                uiState.error != null && uiState.items.isEmpty() -> ErrorContent(onRetry = viewModel::refresh)
                else -> SearchContent(
                    uiState = uiState,
                    isDarkTheme = when (themeMode) {
                        ThemeMode.Light -> false
                        ThemeMode.Dark -> true
                        ThemeMode.System -> isSystemInDarkTheme()
                    },
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onAreaChange = viewModel::onAreaChange,
                    onTypeToggle = viewModel::onTypeToggle,
                    onBudgetChange = viewModel::onBudgetChange,
                    onSubmitNow = viewModel::submitNow,
                    onClearFilters = viewModel::clearFilters,
                    onLoadMore = viewModel::loadNextPage,
                    onListingClick = onListingClick,
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
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
private fun SearchBottomBar(onNavigateToHome: () -> Unit, onNavigateToAbout: () -> Unit, onNavigateToContact: () -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = false,
            onClick = onNavigateToHome,
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
            selected = true,
            onClick = {},
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
private fun SearchContent(
    uiState: SearchUiState,
    isDarkTheme: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onAreaChange: (String?) -> Unit,
    onTypeToggle: (String) -> Unit,
    onBudgetChange: (Int?, Int?) -> Unit,
    onSubmitNow: () -> Unit,
    onClearFilters: () -> Unit,
    onLoadMore: () -> Unit,
    onListingClick: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 2 } }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isTablet = maxWidth >= 600.dp
    val containerModifier = if (isTablet) {
        Modifier
            .widthIn(max = 900.dp)
            .align(Alignment.TopCenter)
    } else {
        Modifier
    }

    Box(modifier = containerModifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
    ) {
        // Header with gradient — includes title, search field, and filters
        item {
            val gradientBrush = if (isDarkTheme) {
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFF3A1D0E),
                        0.55f to Color(0xFF2A1408),
                        1f to Color(0xFF160901),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            } else {
                Brush.linearGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFF8C42),
                        0.55f to Color(0xFFE5722B),
                        1f to Color(0xFF7A2D0A),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(gradientBrush)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Search icon badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (isDarkTheme) Color(0xFFFFD54F).copy(alpha = 0.9f)
                                    else Color(0xFFFDE68A),
                                    shape = RoundedCornerShape(16.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFF7A2D0A) else Color(0xFFC2410C),
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Search Listings",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = "Browse ${uiState.total} verified listings across Cagayan de Oro.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Search field
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "keywords, amenities (e.g. wifi), 1 bed, 2 baths, 30sqm",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDarkTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                            focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                            unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
                            focusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            onSubmitNow()
                        }),
                    )

                    Spacer(Modifier.height(10.dp))

                    // Filters row: Budget + Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BudgetFilter(
                            budgetMin = uiState.budgetMin,
                            budgetMax = uiState.budgetMax,
                            onBudgetChange = onBudgetChange,
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme,
                        )
                        AreaFilter(
                            areas = uiState.catalogs?.barangays ?: emptyList(),
                            selectedArea = uiState.selectedArea,
                            onAreaChange = onAreaChange,
                            modifier = Modifier.weight(1f),
                            isDarkTheme = isDarkTheme,
                        )
                    }
                }
            }
        }

        // Loading indicator when fetching
        if (uiState.isLoading && uiState.items.isNotEmpty()) {
            item {
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Orange500,
                )
            }
        }

        // Type chips
        item {
            val types = uiState.catalogs?.listingTypes ?: emptyList()
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = uiState.selectedTypes.isEmpty(),
                        onClick = {
                            uiState.selectedTypes.forEach { onTypeToggle(it) }
                        },
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
                            selected = uiState.selectedTypes.isEmpty(),
                        ),
                    )
                }
                items(types) { type ->
                    FilterChip(
                        selected = type.value in uiState.selectedTypes,
                        onClick = { onTypeToggle(type.value) },
                        label = {
                            Text(
                                text = type.label,
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
                            selected = type.value in uiState.selectedTypes,
                        ),
                    )
                }
            }
        }

        // Results count + clear filters
        item {
            if (uiState.items.isNotEmpty()) {
                val hasFilters = uiState.searchQuery.isNotBlank() ||
                    uiState.selectedArea != null ||
                    uiState.selectedTypes.isNotEmpty() ||
                    uiState.budgetMin != null ||
                    uiState.budgetMax != null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${uiState.items.size} of ${uiState.total} listings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (hasFilters) {
                        TextButton(onClick = onClearFilters) {
                            Text(
                                text = "Clear filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = Orange500,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        // Listing cards
        if (isTablet) {
            items(uiState.items.chunked(2), key = { "search_row_${it.first().uuid}" }) { row ->
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
            items(uiState.items, key = { "search_${it.uuid}" }) { listing ->
                ListingCard(listing, onClick = { onListingClick(listing.uuid) })
            }
        }

        // Load more / end states
        item {
            when {
                uiState.isLoadingMore -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Orange500, modifier = Modifier.size(32.dp))
                    }
                }
                uiState.hasMore && uiState.items.isNotEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Button(
                            onClick = onLoadMore,
                            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Load More")
                        }
                    }
                }
                !uiState.hasMore && uiState.items.isNotEmpty() -> {
                    Text(
                        text = "No more listings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
                !uiState.isLoading && uiState.items.isEmpty() -> {
                    val hasFilters = uiState.searchQuery.isNotBlank() ||
                        uiState.selectedArea != null ||
                        uiState.selectedTypes.isNotEmpty() ||
                        uiState.budgetMin != null ||
                        uiState.budgetMax != null
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No listings found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (hasFilters) {
                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = onClearFilters,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, Orange500),
                                ) {
                                    Text(
                                        text = "Clear filters",
                                        color = Orange500,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }

    AnimatedVisibility(
        visible = showScrollToTop,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
    ) {
        FloatingActionButton(
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            containerColor = Orange500,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Scroll to top")
        }
    }
    } // Box
    } // BoxWithConstraints
}


@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Orange500)
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
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
