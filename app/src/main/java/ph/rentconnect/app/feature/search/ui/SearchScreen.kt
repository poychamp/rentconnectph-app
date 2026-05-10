package ph.rentconnect.app.feature.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ph.rentconnect.app.R
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.feature.home.data.CatalogItem
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
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SearchTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle) },
        bottomBar = { SearchBottomBar(onNavigateToHome = onNavigateToHome) },
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
private fun SearchBottomBar(onNavigateToHome: () -> Unit) {
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
            onClick = {},
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
            onClick = {},
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

    LazyColumn(
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
                                color = Color.Gray,
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
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
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
                        )
                        AreaFilter(
                            areas = uiState.catalogs?.barangays ?: emptyList(),
                            selectedArea = uiState.selectedArea,
                            onAreaChange = onAreaChange,
                            modifier = Modifier.weight(1f),
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
        items(uiState.items, key = { "search_${it.uuid}" }) { listing ->
            ListingCard(listing, onClick = { onListingClick(listing.uuid) })
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
                        }
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun BudgetFilter(
    budgetMin: Int?,
    budgetMax: Int?,
    onBudgetChange: (Int?, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var minText by remember { mutableStateOf(budgetMin?.toString() ?: "") }
    var maxText by remember { mutableStateOf(budgetMax?.toString() ?: "20000") }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = when {
                budgetMin != null && budgetMax != null -> "₱${"%,d".format(budgetMin)}–₱${"%,d".format(budgetMax)}"
                budgetMin != null -> "₱${"%,d".format(budgetMin)}+"
                budgetMax != null -> "Up to ₱${"%,d".format(budgetMax)}"
                else -> ""
            },
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Budget", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            enabled = true,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also {
                androidx.compose.runtime.LaunchedEffect(it) {
                    it.interactions.collect { interaction ->
                        if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            expanded = true
                        }
                    }
                }
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onBudgetChange(minText.toIntOrNull(), maxText.toIntOrNull())
            },
        ) {
            Column(modifier = Modifier.padding(16.dp).width(280.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = { expanded = false }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Range slider
                val sliderMin = 0f
                val sliderMax = 100000f
                var sliderRange by remember {
                    mutableStateOf(
                        (budgetMin?.toFloat() ?: sliderMin)..(budgetMax?.toFloat() ?: 20000f),
                    )
                }
                RangeSlider(
                    value = sliderRange,
                    onValueChange = { range ->
                        sliderRange = range
                        minText = range.start.toInt().toString()
                        maxText = if (range.endInclusive >= sliderMax) "" else range.endInclusive.toInt().toString()
                    },
                    valueRange = sliderMin..sliderMax,
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = Orange500,
                        activeTrackColor = Orange500,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))

                // Min / Max fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = minText,
                            onValueChange = {
                                minText = it.filter { c -> c.isDigit() }
                                val v = minText.toFloatOrNull() ?: sliderMin
                                sliderRange = v..sliderRange.endInclusive
                            },
                            placeholder = { Text("0") },
                            suffix = { Text("PHP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Max",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = maxText,
                            onValueChange = {
                                maxText = it.filter { c -> c.isDigit() }
                                val v = maxText.toFloatOrNull() ?: sliderMax
                                sliderRange = sliderRange.start..v
                            },
                            placeholder = { Text("No limit") },
                            suffix = { Text("PHP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Clear / Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = {
                        minText = ""
                        maxText = ""
                        sliderRange = sliderMin..sliderMax
                        onBudgetChange(null, null)
                        expanded = false
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            onBudgetChange(minText.toIntOrNull(), maxText.toIntOrNull())
                            expanded = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@Composable
private fun AreaFilter(
    areas: List<CatalogItem>,
    selectedArea: String?,
    onAreaChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = areas.find { it.value == selectedArea }?.label ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("CDO Areas", style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }.also {
                androidx.compose.runtime.LaunchedEffect(it) {
                    it.interactions.collect { interaction ->
                        if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                            expanded = true
                        }
                    }
                }
            },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("All Areas") },
                onClick = {
                    onAreaChange(null)
                    expanded = false
                },
            )
            areas.forEach { area ->
                DropdownMenuItem(
                    text = { Text(area.label) },
                    onClick = {
                        onAreaChange(area.value)
                        expanded = false
                    },
                )
            }
        }
    }
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
