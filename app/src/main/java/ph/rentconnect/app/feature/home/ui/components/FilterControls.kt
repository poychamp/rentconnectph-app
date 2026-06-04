package ph.rentconnect.app.feature.home.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ph.rentconnect.app.feature.home.data.CatalogItem
import ph.rentconnect.app.ui.theme.Orange500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetFilter(
    budgetMin: Int?,
    budgetMax: Int?,
    onBudgetChange: (Int?, Int?) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
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
            placeholder = { Text("Budget", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
                focusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            enabled = true,
            interactionSource = remember { MutableInteractionSource() }.also {
                LaunchedEffect(it) {
                    it.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
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
fun AreaFilter(
    areas: List<CatalogItem>,
    selectedArea: String?,
    onAreaChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = areas.find { it.value == selectedArea }?.label ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("CDO Areas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
                focusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.outline else Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            interactionSource = remember { MutableInteractionSource() }.also {
                LaunchedEffect(it) {
                    it.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
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
                    text = { Text(area.label ?: "") },
                    onClick = {
                        onAreaChange(area.value)
                        expanded = false
                    },
                )
            }
        }
    }
}
