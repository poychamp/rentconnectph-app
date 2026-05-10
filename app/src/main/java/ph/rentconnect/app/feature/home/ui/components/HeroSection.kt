package ph.rentconnect.app.feature.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.rentconnect.app.R
import ph.rentconnect.app.feature.home.data.CatalogItem

@Composable
fun HeroSection(
    barangays: List<CatalogItem>,
    modifier: Modifier = Modifier,
) {
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
                value = "",
                onValueChange = {},
                readOnly = true,
                placeholder = {
                    Text(
                        text = "keywords, amenities (e.g. wifi), 1 bed, 2 baths, 30sqm",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
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
            )

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DropdownChip(
                    label = "Budget",
                    items = emptyList(),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                DropdownChip(
                    label = "CDO Areas",
                    items = barangays,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DropdownChip(
    label: String,
    items: List<CatalogItem>,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<CatalogItem?>(null) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .clickable(enabled = items.isNotEmpty()) { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selected?.label ?: label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected != null) Color.Black else Color.Gray,
                modifier = Modifier.weight(1f),
            )
            if (items.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        selected = item
                        expanded = false
                    },
                )
            }
        }
    }
}
