package ph.rentconnect.app.feature.detail.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.rentconnect.app.R
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.feature.detail.data.InquiryContactInfo
import ph.rentconnect.app.ui.gesture.swipeToBack
import ph.rentconnect.app.ui.theme.Orange500

@Composable
fun InquirySuccessScreen(
    contactInfo: InquiryContactInfo,
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
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
        },
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .swipeToBack(
                    onDismiss = onBack,
                    startZoneWidth = 9999.dp,
                ),
        ) {
            val containerModifier = if (maxWidth >= 600.dp) {
                Modifier
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = containerModifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Success icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Orange500, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp),
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "You're all set",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Here's how to reach out about ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                            append(contactInfo.listingTitle)
                        }
                        append(".")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                // Contact card
                ContactCard(
                    contactInfo = contactInfo,
                    context = context,
                )

                Spacer(Modifier.height(24.dp))

                // Back to listing button (grey secondary)
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        text = "← Back to listing",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contactInfo: InquiryContactInfo,
    context: Context,
) {
    val phone = contactInfo.contactPhone

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header label: "CONTACT NUMBER · OWNER"
            val headerLabel = buildString {
                append("CONTACT NUMBER")
                if (!contactInfo.contactTypeLabel.isNullOrBlank()) {
                    append(" · ")
                    append(contactInfo.contactTypeLabel.uppercase())
                }
            }
            Text(
                text = headerLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )

            if (phone != null) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = formatPhoneForDisplay(phone),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(16.dp))

                // Copy + Call buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CopyButton(
                        phone = bareLocalPhone(phone),
                        context = context,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Call",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Ask for
            if (!contactInfo.contactName.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                InfoRow(label = "ASK FOR", value = contactInfo.contactName)
            }

            // Notes
            if (!contactInfo.contactNotes.isNullOrBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
                InfoRow(label = "NOTES", value = contactInfo.contactNotes)
            }

            // Barangay
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            InfoRow(label = "BARANGAY", value = contactInfo.barangay)
        }
    }
}

@Composable
private fun CopyButton(
    phone: String,
    context: Context,
    modifier: Modifier = Modifier,
) {
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.5f else 1.0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 80, easing = androidx.compose.animation.core.EaseOut),
        label = "press-alpha",
    )

    OutlinedButton(
        onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Phone number", phone))
            copied = true
            scope.launch {
                delay(2000)
                copied = false
            }
        },
        modifier = modifier.alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Orange500,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Orange500),
    ) {
        Icon(
            imageVector = if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Orange500,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (copied) "Copied!" else "Copy",
            fontWeight = FontWeight.SemiBold,
            color = Orange500,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun formatPhoneForDisplay(e164: String): String {
    val local = bareLocalPhone(e164)
    return if (local.length == 11) {
        "${local.substring(0, 4)} ${local.substring(4, 7)} ${local.substring(7)}"
    } else {
        local
    }
}

private fun bareLocalPhone(e164: String): String {
    val digits = e164.replace(Regex("\\D"), "")
    return when {
        digits.length == 12 && digits.startsWith("63") -> "0${digits.substring(2)}"
        digits.length == 11 && digits.startsWith("0") -> digits
        digits.length == 10 && digits.startsWith("9") -> "0$digits"
        else -> digits
    }
}
