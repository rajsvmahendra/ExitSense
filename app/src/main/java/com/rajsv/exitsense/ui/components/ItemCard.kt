package com.rajsv.exitsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.model.ReminderItem
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import kotlin.math.roundToInt

@Composable
fun ItemCard(
    item: ReminderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    enableSwipe: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colorScheme = MaterialTheme.colorScheme

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "item_card_scale"
    )

    val shape = RoundedCornerShape(20.dp)

    val importanceColor = when (item.importance) {
        ImportanceLevel.CRITICAL -> colorScheme.error
        ImportanceLevel.HIGH -> ExitSenseTheme.customColors.statusWarning
        ImportanceLevel.MEDIUM -> colorScheme.primary
        ImportanceLevel.LOW -> colorScheme.secondary
    }

    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 200),
        label = "swipe_offset"
    )

    val swipeThreshold = 100f
    val maxSwipe = 220f

    val actionAlpha by animateFloatAsState(
        targetValue = if (offsetX < -swipeThreshold / 2) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "action_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        // Delete/Edit background
        if (enableSwipe && (onDelete != null || onEdit != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(shape)
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Buttons
                if (onPin != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (item.isPinned) colorScheme.secondary.copy(alpha = actionAlpha) else colorScheme.primary.copy(alpha = actionAlpha))
                            .clickable {
                                offsetX = 0f
                                onPin()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (onEdit != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.primary.copy(alpha = actionAlpha))
                            .clickable {
                                offsetX = 0f
                                onEdit()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (onDelete != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.error.copy(alpha = actionAlpha))
                            .clickable {
                                offsetX = 0f
                                onDelete()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        // Main card content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = ExitSenseTheme.customColors.shadow,
                    spotColor = ExitSenseTheme.customColors.shadow
                )
                .clip(shape)
                .background(colorScheme.primaryContainer)
                .border(
                    width = 1.dp,
                    color = ExitSenseTheme.customColors.cardBorder.copy(alpha = 0.3f),
                    shape = shape
                )
                .then(
                    if (enableSwipe && (onDelete != null || onEdit != null)) {
                        Modifier.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX < -swipeThreshold) {
                                        offsetX = -maxSwipe
                                    } else {
                                        offsetX = 0f
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    offsetX = (offsetX + dragAmount).coerceIn(-maxSwipe, 0f)
                                }
                            )
                        }
                    } else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (offsetX != 0f) {
                            offsetX = 0f
                        } else {
                            onClick()
                        }
                    }
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Container
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(importanceColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon.icon,
                        contentDescription = item.name,
                        tint = importanceColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = ExitSenseTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (item.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.locationName != null) {
                            Icon(
                                imageVector = Icons.Outlined.Place,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.locationName,
                                style = ExitSenseTypography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (item.reminderTime != null) {
                            if (item.locationName != null) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.reminderTime,
                                style = ExitSenseTypography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Importance Indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(importanceColor)
                )
            }
        }
    }
}

@Composable
fun CompactItemCard(
    item: ReminderItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val colorScheme = MaterialTheme.colorScheme

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "compact_item_scale"
    )

    val shape = RoundedCornerShape(16.dp)

    val importanceColor = when (item.importance) {
        ImportanceLevel.CRITICAL -> colorScheme.error
        ImportanceLevel.HIGH -> ExitSenseTheme.customColors.statusWarning
        ImportanceLevel.MEDIUM -> colorScheme.primary
        ImportanceLevel.LOW -> colorScheme.secondary
    }

    Column(
        modifier = modifier
            .width(100.dp)
            .scale(scale)
            .clip(shape)
            .background(colorScheme.primaryContainer)
            .border(
                width = 1.dp,
                color = ExitSenseTheme.customColors.cardBorder.copy(alpha = 0.3f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(importanceColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon.icon,
                contentDescription = item.name,
                tint = importanceColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = item.name,
            style = ExitSenseTypography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
