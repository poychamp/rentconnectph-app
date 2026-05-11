package ph.rentconnect.app.feature.about.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ph.rentconnect.app.R
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.ui.theme.Orange500

@Composable
fun AboutScreen(
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToContact: () -> Unit,
    onBrowseListings: () -> Unit,
) {
    Scaffold(
        topBar = { AboutTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle) },
        bottomBar = {
            AboutBottomBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToContact = onNavigateToContact,
            )
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            val isTablet = maxWidth >= 600.dp
            val containerModifier = if (isTablet) {
                Modifier
                    .widthIn(max = 900.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier
            }

            Column(
                modifier = containerModifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                AboutHeroSection(onBrowseListings = onBrowseListings)
                OurStorySection(isTablet = isTablet)
                HowItWorksSection(isTablet = isTablet)
                MeetTheBrokerSection(isTablet = isTablet)
                FeatureCardsSection(isTablet = isTablet)
                CtaBannerSection(onBrowseListings = onBrowseListings, isTablet = isTablet)
            }
        }
    }
}

@Composable
private fun AboutTopBar(
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
private fun AboutHeroSection(onBrowseListings: () -> Unit) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val gradient = if (isLight) {
        Brush.verticalGradient(listOf(Color(0xFFFF8C42), Color(0xFFE5722B), Color(0xFF7A2D0A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF3A1D0E), Color(0xFF2A1408), Color(0xFF160901)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(gradient)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        // "CAGAYAN DE ORO" badge
        Box(
            modifier = Modifier
                .background(Color(0xFF1F2937), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF22C55E), CircleShape),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "CAGAYAN DE ORO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                append("Rentals in CDO, ")
                val emphasisColor = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
                    Color(0xFFFDE585) else Orange500
                withStyle(SpanStyle(color = emphasisColor)) {
                    append("without the guesswork.")
                }
            },
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "RentConnectPH is a verified-listings platform for renters and property owners in Cagayan de Oro. We built it to give renters a single, reliable place to find a home \u2014 and to give owners a trusted channel to reach them.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBrowseListings,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
            ) {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text("Browse listings", color = Color.White)
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                ),
            ) {
                Text("How it works", color = Color.White)
            }
        }
    }
}

@Composable
private fun OurStorySection(isTablet: Boolean = false) {
    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OUR STORY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Orange500,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Built for renters\nand CDO property owners.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "Cagayan de Oro\u2019s rental market is fragmented across Facebook Marketplace, classified groups, and informal listings. Renters spend hours sorting through unreliable posts, often without a clear way to verify what is available or who they are dealing with.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "RentConnectPH centralizes verified rental listings from CDO property owners into a single platform \u2014 searchable, filterable, with clear photos and PHP pricing. Owners reach renters through a trusted channel. Renters get a vetted shortlist, with a licensed broker handling the lease at closing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = "OUR STORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Orange500,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Built for renters\nand CDO property owners.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Cagayan de Oro\u2019s rental market is fragmented across Facebook Marketplace, classified groups, and informal listings. Renters spend hours sorting through unreliable posts, often without a clear way to verify what is available or who they are dealing with.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "RentConnectPH centralizes verified rental listings from CDO property owners into a single platform \u2014 searchable, filterable, with clear photos and PHP pricing. Owners reach renters through a trusted channel. Renters get a vetted shortlist, with a licensed broker handling the lease at closing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HowItWorksSection(isTablet: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "HOW IT WORKS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Orange500,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Three steps from search to signing.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(20.dp))
        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StepCard(
                    step = "STEP 01",
                    title = "Search verified listings",
                    description = "Every listing on RentConnectPH is reviewed by our team before publishing. Photos, address, and pricing are verified, so what you see reflects what is actually available.",
                    modifier = Modifier.weight(1f),
                )
                StepCard(
                    step = "STEP 02",
                    title = "Send an inquiry",
                    description = "Submit an inquiry on any listing with your move-in date and basic requirements. Our team contacts you to confirm details and arranges a viewing with the property owner.",
                    modifier = Modifier.weight(1f),
                )
                StepCard(
                    step = "STEP 03",
                    title = "Sign with a licensed broker",
                    description = "When you\u2019re ready to commit, our PRC-licensed broker handles the closing \u2014 drafting the lease, negotiating terms, and documenting deposits and fees clearly.",
                    modifier = Modifier.weight(1f),
                )
            }
        } else {
            StepCard(
                step = "STEP 01",
                title = "Search verified listings",
                description = "Every listing on RentConnectPH is reviewed by our team before publishing. Photos, address, and pricing are verified, so what you see reflects what is actually available.",
            )
            Spacer(Modifier.height(12.dp))
            StepCard(
                step = "STEP 02",
                title = "Send an inquiry",
                description = "Submit an inquiry on any listing with your move-in date and basic requirements. Our team contacts you to confirm details and arranges a viewing with the property owner.",
            )
            Spacer(Modifier.height(12.dp))
            StepCard(
                step = "STEP 03",
                title = "Sign with a licensed broker",
                description = "When you\u2019re ready to commit, our PRC-licensed broker handles the closing \u2014 drafting the lease, negotiating terms, and documenting deposits and fees clearly.",
            )
        }
    }
}

@Composable
private fun StepCard(step: String, title: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Orange500,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MeetTheBrokerSection(isTablet: Boolean = false) {
    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.broker),
                contentDescription = "Marco V. Reyes",
                modifier = Modifier
                    .weight(1f)
                    .height(320.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "MEET THE BROKER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Orange500,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Marco V. Reyes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "PRC-Licensed Real Estate Broker \u00B7 PRB Lic. #0028451",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "PAREB-CDO Chapter Member",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF22C55E),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Marco has spent the last twelve years brokering rentals across Cagayan de Oro, with experience spanning studio condos in Pueblo de Oro, family homes in Carmen, and properties throughout Lapasan. His practice centers on what tenants prioritize: fair pricing, responsive landlords, and clear terms at move\u2011in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "He partnered with RentConnectPH because the verification model aligns with how he prefers to broker \u2014 owners vetted up front, paperwork executed properly at closing, and a structured process throughout. Every lease that closes through the platform is drafted and witnessed by Marco personally, ensuring accountability on every contract.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.broker),
                contentDescription = "Marco V. Reyes",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "MEET THE BROKER",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Orange500,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Marco V. Reyes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "PRC-Licensed Real Estate Broker \u00B7 PRB Lic. #0028451",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "PAREB-CDO Chapter Member",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF22C55E),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Marco has spent the last twelve years brokering rentals across Cagayan de Oro, with experience spanning studio condos in Pueblo de Oro, family homes in Carmen, and properties throughout Lapasan. His practice centers on what tenants prioritize: fair pricing, responsive landlords, and clear terms at move\u2011in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "He partnered with RentConnectPH because the verification model aligns with how he prefers to broker \u2014 owners vetted up front, paperwork executed properly at closing, and a structured process throughout. Every lease that closes through the platform is drafted and witnessed by Marco personally, ensuring accountability on every contract.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FeatureCardsSection(isTablet: Boolean = false) {
    if (isTablet) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    title = "Cagayan de Oro\u2013first",
                    description = "Focused on CDO neighborhoods including Pueblo de Oro, Carmen, Kauswagan, and Lapasan, with continued expansion.",
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    title = "Every listing verified",
                    description = "Each listing is reviewed by our team before publishing, with photos, address, and pricing verified.",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    title = "Licensed broker on every lease",
                    description = "Every closing is handled by our PRC-licensed broker, who drafts the lease and negotiates final terms.",
                    modifier = Modifier.weight(1f),
                )
                FeatureCard(
                    title = "Transparent pricing",
                    description = "All pricing in PHP. Monthly rent, deposits, and any platform fees disclosed before inquiry.",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FeatureCard(
                title = "Cagayan de Oro\u2013first",
                description = "Focused on CDO neighborhoods including Pueblo de Oro, Carmen, Kauswagan, and Lapasan, with continued expansion.",
            )
            FeatureCard(
                title = "Every listing verified",
                description = "Each listing is reviewed by our team before publishing, with photos, address, and pricing verified.",
            )
            FeatureCard(
                title = "Licensed broker on every lease",
                description = "Every closing is handled by our PRC-licensed broker, who drafts the lease and negotiates final terms.",
            )
            FeatureCard(
                title = "Transparent pricing",
                description = "All pricing in PHP. Monthly rent, deposits, and any platform fees disclosed before inquiry.",
            )
        }
    }
}

@Composable
private fun FeatureCard(title: String, description: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Orange500,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CtaBannerSection(onBrowseListings: () -> Unit, isTablet: Boolean = false) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val gradient = if (isLight) {
        Brush.verticalGradient(listOf(Color(0xFFE5722B), Color(0xFF7A2D0A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF3A1D0E), Color(0xFF2A1408), Color(0xFF160901)))
    }

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Looking for a place in CDO?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Start with our verified listings. Every one is reviewed before it goes live.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            Spacer(Modifier.width(24.dp))
            OutlinedButton(
                onClick = onBrowseListings,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
            ) {
                Text("Browse verified listings", color = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("\u2192", color = Color.White)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            Text(
                text = "Looking for a place in CDO?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Start with our verified listings. Every one is reviewed before it goes live.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(20.dp))
            OutlinedButton(
                onClick = onBrowseListings,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
            ) {
                Text("Browse verified listings", color = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("\u2192", color = Color.White)
            }
        }
    }
}

@Composable
private fun AboutBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToContact: () -> Unit,
) {
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
            selected = true,
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
