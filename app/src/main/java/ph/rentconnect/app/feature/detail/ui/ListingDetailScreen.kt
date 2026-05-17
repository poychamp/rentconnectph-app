package ph.rentconnect.app.feature.detail.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import ph.rentconnect.app.BuildConfig
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold

import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ph.rentconnect.app.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.feature.detail.data.ListingDetail
import ph.rentconnect.app.feature.detail.ui.components.LocationMapDisplay
import ph.rentconnect.app.ui.components.SecondaryOutlinedButton
import ph.rentconnect.app.ui.gesture.swipeToBack
import ph.rentconnect.app.ui.theme.Green500
import ph.rentconnect.app.ui.theme.Orange500
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    viewModel: ListingDetailViewModel,
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onInquirySuccess: (listingName: String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val inquiryState by viewModel.inquiryState.collectAsStateWithLifecycle()

    val listingName = (uiState as? ListingDetailUiState.Success)?.listing?.title ?: ""

    when (val inquiry = inquiryState) {
        is InquiryDialogState.Visible -> {
            InquiryFormDialog(
                state = inquiry,
                onNameChange = viewModel::updateInquiryName,
                onPhoneChange = viewModel::updateInquiryPhone,
                onFieldFocus = viewModel::onFieldFocus,
                onFieldBlur = viewModel::onFieldBlur,
                onSubmit = viewModel::submitInquiry,
                onDismiss = viewModel::closeInquiryDialog,
            )
        }
        is InquiryDialogState.Success -> {
            viewModel.closeInquiryDialog()
            onInquirySuccess(listingName)
        }
        is InquiryDialogState.Hidden -> {}
    }

    Scaffold(
        topBar = { DetailTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle, onBack = onBack) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .swipeToBack(
                    onDismiss = onBack,
                    startZoneWidth = 9999.dp,
                ),
        ) {
            when (val state = uiState) {
                is ListingDetailUiState.Loading -> LoadingContent(PaddingValues())
                is ListingDetailUiState.NotFound -> NotFoundContent(PaddingValues(), onBack)
                is ListingDetailUiState.Error -> ErrorContent(PaddingValues(), onRetry = viewModel::retry)
                is ListingDetailUiState.Success -> DetailContent(
                    PaddingValues(),
                    state.listing,
                    onInquireClick = viewModel::openInquiryDialog,
                )
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
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
private fun NotFoundContent(padding: PaddingValues, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Listing not found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = onBack) {
                Text("Go back")
            }
        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    padding: PaddingValues,
    listing: ListingDetail,
    onInquireClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var mapTouching by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState(), enabled = !mapTouching),
        ) {
            // Photo gallery
            if (!listing.images.isNullOrEmpty()) {
                PhotoGallery(listing.images.mapNotNull { it.url }, galleryHeight = if (isTablet) 400.dp else 250.dp)
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))

                // Verified badge + share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .background(Green500, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        )
                    }
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${BuildConfig.BASE_URL}/listings/${listing.uuid}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share listing"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(4.dp))

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Orange500,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${listing.barangayLabel ?: "CDO"}, Cagayan de Oro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Price + type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "₱${"%,d".format(listing.priceMonthly ?: 0)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Orange500,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            text = "per month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = listing.typeLabel ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Specs — only show truthy values
                val specs = buildList {
                    listing.beds?.takeIf { it > 0 }?.let { add(Icons.Filled.KingBed to "$it beds") }
                    listing.baths?.takeIf { it > 0 }?.let { add(Icons.Filled.Bathtub to "$it baths") }
                    listing.sqm?.takeIf { it > 0 }?.let { add(Icons.Filled.CropSquare to "$it sqm") }
                }
                if (specs.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        specs.forEach { (icon, text) -> SpecItem(icon, text) }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Verified date
                listing.listedAt?.let { dateStr ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Green500,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Verified on ${formatDate(dateStr)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Green500,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Owner contact never shown publicly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )

                // Description
                if (!listing.description.isNullOrBlank()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "About this property",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp,
                    )
                }

                // Amenities
                if (!listing.amenities.isNullOrEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Amenities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listing.amenities.orEmpty().forEach { amenity ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(20.dp),
                                    )
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = amenity.name ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Map
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(8.dp))
                if (listing.latitude != null && listing.longitude != null) {
                    LocationMapDisplay(
                        latitude = listing.latitude,
                        longitude = listing.longitude,
                        onTouchChanged = { mapTouching = it },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                    )
                    Spacer(Modifier.height(8.dp))
                    val mapsUrl = "https://www.google.com/maps/search/?api=1&query=${listing.latitude},${listing.longitude}"
                    SecondaryOutlinedButton(
                        text = "Open in Google Maps",
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl)))
                        },
                        isCompact = false,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No coordinates available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Bottom spacer for sticky button
                Spacer(Modifier.height(80.dp))
            }
        }

        // Sticky Inquire Now button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                onClick = onInquireClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange500),
            ) {
                Text(
                    text = "Inquire Now  →",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
    } // BoxWithConstraints
}

@Composable
private fun PhotoGallery(imageUrls: List<String>, galleryHeight: Dp = 250.dp) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(galleryHeight),
        ) { page ->
            AsyncImage(
                model = imageUrls[page],
                contentDescription = "Photo ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Thumbnails
        if (imageUrls.size > 1) {
            val scope = rememberCoroutineScope()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                imageUrls.forEachIndexed { index, url ->
                    AsyncImage(
                        model = url,
                        contentDescription = "Thumbnail ${index + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (index == pagerState.currentPage)
                                    Modifier.background(
                                        Orange500.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp),
                                    )
                                else Modifier
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InquiryFormDialog(
    state: InquiryDialogState.Visible,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onFieldFocus: (String) -> Unit,
    onFieldBlur: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    var nameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!state.isSubmitting) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .widthIn(min = 340.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Inquire about this listing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !state.isSubmitting,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Name field
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused) { nameTouched = true; onFieldFocus("name") }
                            else if (nameTouched) onFieldBlur("name")
                        },
                    enabled = !state.isSubmitting,
                    isError = state.fieldErrors.containsKey("name"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )
                state.fieldErrors["name"]?.firstOrNull()?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Phone field
                Text(
                    text = "Phone",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged {
                            if (it.isFocused) { phoneTouched = true; onFieldFocus("phone") }
                            else if (phoneTouched) onFieldBlur("phone")
                        },
                    enabled = !state.isSubmitting,
                    isError = state.fieldErrors.containsKey("phone"),
                    singleLine = true,
                    placeholder = { Text("0917 123 4567") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(8.dp),
                )
                state.fieldErrors["phone"]?.firstOrNull()?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Our team will give you a call shortly.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Submit error
                state.submitError?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (state.isSubmitting) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Orange500,
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                ) {
                    Text(
                        text = "Submit inquiry",
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}


private fun formatDate(isoDate: String): String =
    try {
        val odt = OffsetDateTime.parse(isoDate)
        odt.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH))
    } catch (_: Exception) {
        isoDate
    }
