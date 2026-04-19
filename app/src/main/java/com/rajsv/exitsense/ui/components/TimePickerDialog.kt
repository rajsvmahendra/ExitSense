package com.rajsv.exitsense.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajsv.exitsense.ui.theme.ExitSenseTypography

@Composable
fun TimePickerDialog(
    initialHour: Int = 8,
    initialMinute: Int = 30,
    onDismiss: () -> Unit,
    onTimeSelected: (hour: Int, minute: Int, amPm: String) -> Unit
) {
    var selectedHour by remember { mutableIntStateOf(if (initialHour > 12) initialHour - 12 else if (initialHour == 0) 12 else initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }
    var selectedAmPm by remember { mutableIntStateOf(if (initialHour >= 12) 1 else 0) } // 0 = AM, 1 = PM

    val colorScheme = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surface,
        title = {
            Text(
                text = "Select Time",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = String.format("%02d", selectedHour),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = ":",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = String.format("%02d", selectedMinute),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedAmPm == 0) "AM" else "PM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hour Selection
                Text(
                    text = "Hour",
                    style = ExitSenseTypography.labelMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                HourSelector(
                    selectedHour = selectedHour,
                    onHourSelected = { selectedHour = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Minute Selection
                Text(
                    text = "Minute",
                    style = ExitSenseTypography.labelMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                MinuteSelector(
                    selectedMinute = selectedMinute,
                    onMinuteSelected = { selectedMinute = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // AM/PM Selection
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AmPmButton(
                        text = "AM",
                        isSelected = selectedAmPm == 0,
                        onClick = { selectedAmPm = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    AmPmButton(
                        text = "PM",
                        isSelected = selectedAmPm == 1,
                        onClick = { selectedAmPm = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amPmText = if (selectedAmPm == 0) "AM" else "PM"
                    onTimeSelected(selectedHour, selectedMinute, amPmText)
                }
            ) {
                Text("Confirm", color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun HourSelector(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        (1..12).forEach { hour ->
            TimeChip(
                text = hour.toString(),
                isSelected = selectedHour == hour,
                onClick = { onHourSelected(hour) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MinuteSelector(
    selectedMinute: Int,
    onMinuteSelected: (Int) -> Unit
) {
    val minutes = listOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            minutes.take(6).forEach { minute ->
                TimeChip(
                    text = String.format("%02d", minute),
                    isSelected = selectedMinute == minute,
                    onClick = { onMinuteSelected(minute) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            minutes.drop(6).forEach { minute ->
                TimeChip(
                    text = String.format("%02d", minute),
                    isSelected = selectedMinute == minute,
                    onClick = { onMinuteSelected(minute) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colorScheme.primary else colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitSenseTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AmPmButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colorScheme.primary else colorScheme.primaryContainer)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = ExitSenseTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) colorScheme.onPrimary else colorScheme.onSurface
        )
    }
}
