package com.rajsv.exitsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rajsv.exitsense.ui.theme.ExitSenseTypography

@Composable
fun PremiumChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> colorScheme.primary.copy(alpha = 0.15f)
            else -> colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 200),
        label = "chip_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(durationMillis = 200),
        label = "chip_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "chip_text"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "chip_scale"
    )

    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = ExitSenseTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 200),
        label = "day_chip_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "day_chip_text"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "day_chip_scale"
    )

    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .size(44.dp)
            .scale(scale)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.3f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = ExitSenseTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun PremiumToggle(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    val thumbOffset by animateDpAsState(
        targetValue = if (isChecked) 22.dp else 2.dp,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_offset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (isChecked) colorScheme.primary else colorScheme.outline,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_track"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (isChecked) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "toggle_thumb"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(
                text = label,
                style = ExitSenseTypography.bodyMedium,
                color = colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
        }

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(trackColor)
                .clickable { onCheckedChange(!isChecked) }
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .padding(vertical = 2.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(thumbColor),
                contentAlignment = Alignment.Center
            ) {
                if (isChecked) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImportanceSelector(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    val levels = listOf("Low", "Medium", "High", "Critical")
    val colors = listOf(
        colorScheme.secondary,
        colorScheme.primary,
        MaterialTheme.colorScheme.tertiary, // Using tertiary for High (Warningish)
        colorScheme.error
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Importance",
            style = ExitSenseTypography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            levels.forEachIndexed { index, level ->
                val isSelected = selectedLevel == index

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) colors[index].copy(alpha = 0.2f) else colorScheme.primaryContainer,
                    animationSpec = tween(durationMillis = 200),
                    label = "importance_bg_$index"
                )

                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) colors[index] else colorScheme.outline.copy(alpha = 0.3f),
                    animationSpec = tween(durationMillis = 200),
                    label = "importance_border_$index"
                )

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1f,
                    animationSpec = tween(durationMillis = 150),
                    label = "importance_scale_$index"
                )

                val shape = RoundedCornerShape(12.dp)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clip(shape)
                        .background(backgroundColor)
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = shape
                        )
                        .clickable { onLevelSelected(index) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level,
                        style = ExitSenseTypography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) colors[index] else colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
