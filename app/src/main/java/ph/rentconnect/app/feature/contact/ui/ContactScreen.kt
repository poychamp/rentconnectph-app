package ph.rentconnect.app.feature.contact.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ph.rentconnect.app.core.persistence.ThemeMode
import ph.rentconnect.app.ui.theme.Orange500

@Composable
fun ContactScreen(
    viewModel: ContactViewModel,
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onContactSuccess: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            ContactTopBar(themeMode = themeMode, onThemeToggle = onThemeToggle)
        },
        bottomBar = {
            ContactBottomBar(
                onNavigateToHome = onNavigateToHome,
                onNavigateToSearch = onNavigateToSearch,
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is ContactUiState.Form -> ContactFormContent(
                state = state,
                modifier = Modifier.padding(padding),
                onNameChange = viewModel::updateName,
                onEmailChange = viewModel::updateEmail,
                onMessageChange = viewModel::updateMessage,
                onFieldFocus = viewModel::onFieldFocus,
                onFieldBlur = viewModel::onFieldBlur,
                onSubmit = viewModel::submitContact,
            )
            is ContactUiState.Success -> {
                LaunchedEffect(Unit) {
                    onContactSuccess()
                }
            }
        }
    }
}

@Composable
private fun ContactTopBar(
    themeMode: ThemeMode,
    onThemeToggle: suspend (ThemeMode) -> Unit,
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
        // Spacer to balance the theme toggle on the right
        Box(modifier = Modifier.size(48.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun ContactFormContent(
    state: ContactUiState.Form,
    modifier: Modifier = Modifier,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onFieldFocus: (String) -> Unit,
    onFieldBlur: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    var nameTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var messageTouched by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        // Mail icon badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    if (MaterialTheme.colorScheme.surface.luminance() > 0.5f)
                        Color(0xFFFFEDD4) else Color(0x4D7E2B0D),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(28.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Contact us",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Got a question, want to list a property, or just want to say hi? Send us a note.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        // Name field
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.PersonOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.padding(start = 6.dp))
            Text(
                text = "Name",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Email field
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.MailOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.padding(start = 6.dp))
            Text(
                text = "Email",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) { emailTouched = true; onFieldFocus("email") }
                    else if (emailTouched) onFieldBlur("email")
                },
            enabled = !state.isSubmitting,
            isError = state.fieldErrors.containsKey("email"),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(8.dp),
        )
        state.fieldErrors["email"]?.firstOrNull()?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Message field
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.padding(start = 6.dp))
            Text(
                text = "Message",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = state.message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .onFocusChanged {
                    if (it.isFocused) { messageTouched = true; onFieldFocus("message") }
                    else if (messageTouched) onFieldBlur("message")
                },
            enabled = !state.isSubmitting,
            isError = state.fieldErrors.containsKey("message"),
            shape = RoundedCornerShape(8.dp),
        )
        state.fieldErrors["message"]?.firstOrNull()?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }

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
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange500),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.padding(start = 8.dp))
            Text(
                text = "Send message",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ContactBottomBar(
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
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
            selected = true,
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
