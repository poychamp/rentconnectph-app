package ph.rentconnect.app.feature.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.rentconnect.app.R
import ph.rentconnect.app.feature.home.data.CatalogItem

@Composable
fun HeroSection(
    barangays: List<CatalogItem>,
    searchQuery: String,
    selectedArea: String?,
    budgetMin: Int?,
    budgetMax: Int?,
    onSearchQueryChange: (String) -> Unit,
    onAreaChange: (String?) -> Unit,
    onBudgetChange: (Int?, Int?) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.cdo_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Cagayan de Oro \u00B7 Philippines",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFFE3B0),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Find your next home in CDO",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Verified listings you can trust.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )

            Spacer(Modifier.weight(1f))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "keywords, amenities (e.g. wifi), 1 bed, 2 baths, 30sqm",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkTheme) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
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
                    onSubmit()
                }),
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BudgetFilter(
                    budgetMin = budgetMin,
                    budgetMax = budgetMax,
                    onBudgetChange = onBudgetChange,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme,
                )
                AreaFilter(
                    areas = barangays,
                    selectedArea = selectedArea,
                    onAreaChange = onAreaChange,
                    modifier = Modifier.weight(1f),
                    isDarkTheme = isDarkTheme,
                )
            }
        }
    }
}
