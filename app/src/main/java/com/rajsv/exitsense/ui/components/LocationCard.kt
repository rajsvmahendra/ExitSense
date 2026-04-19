package com.rajsv.exitsense.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
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
import com.rajsv.exitsense.data.model.Location
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import kotlin.math.roundToInt

@Composable
fun LocationCard(
    location: Location,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onPin: (() -> Unit)? = null,
    enableSwipe: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "location_card_scale"
    )

    val shape = RoundedCornerShape(20.dp)

    // Theme colors
    val colorScheme = MaterialTheme.colorScheme
    val customColors = ExitSenseTheme.customColors
    
    val cardBg = colorScheme.primaryContainer
    val cardBorderColor = customColors.cardBorder
    val textPrimaryColor = colorScheme.onBackground
    val textSecondaryColor = colorScheme.onSurfaceVariant
    val textTertiaryColor = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    val shadowColor = customColors.shadow

    // Swipe state
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 200),
        label = "swipe_offset"
    )

    val swipeThreshold = 100f
    val maxSwipe = 220f

    val deleteBackgroundAlpha by animateFloatAsState(
        targetValue = if (offsetX < -swipeThreshold / 2) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "delete_bg_alpha"
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
                    .height(88.dp)
                    .clip(shape)
                    .background(Color.Transparent),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pin button
                if (onPin != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (location.isPinned) colorScheme.secondary.copy(alpha = deleteBackgroundAlpha) else colorScheme.primary.copy(alpha = deleteBackgroundAlpha))
                            .clickable {
                                offsetX = 0f
                                onPin()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (location.isPinned) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (location.isPinned) colorScheme.onSecondary else colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Edit button
                if (onEdit != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.primary.copy(alpha = deleteBackgroundAlpha))
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

                // Delete button
                if (onDelete != null && offsetX < -swipeThreshold / 2) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.error.copy(alpha = deleteBackgroundAlpha))
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
                    ambientColor = shadowColor,
                    spotColor = shadowColor
                )
                .clip(shape)
                .background(cardBg)
                .border(
                    width = 1.dp,
                    color = cardBorderColor.copy(alpha = 0.3f),
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
                        .background(colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = location.icon.icon,
                        contentDescription = location.name,
                        tint = colorScheme.secondary,
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
                            text = location.name,
                            style = ExitSenseTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (location.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = null,
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (location.address.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = location.address,
                            style = ExitSenseTypography.bodySmall,
                            color = textSecondaryColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Radius Badge
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MyLocation,
                            contentDescription = null,
                            tint = textTertiaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${location.radiusInMeters}m",
                            style = ExitSenseTypography.labelSmall,
                            color = textTertiaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Active Status
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (location.isActive) colorScheme.primary.copy(alpha = 0.15f)
                                else cardBorderColor.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (location.isActive) "Active" else "Inactive",
                            style = ExitSenseTypography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (location.isActive) colorScheme.primary else textTertiaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactLocationCard(
    location: Location,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "compact_location_scale"
    )

    val shape = RoundedCornerShape(14.dp)

    // Theme colors
    val colorScheme = MaterialTheme.colorScheme
    val customColors = ExitSenseTheme.customColors
    
    val cardBg = colorScheme.primaryContainer
    val cardBorderColor = customColors.cardBorder
    val textPrimaryColor = colorScheme.onBackground
    val textSecondaryColor = colorScheme.onSurfaceVariant

    val backgroundColor = if (isSelected) colorScheme.secondary.copy(alpha = 0.15f) else cardBg
    val borderColor = if (isSelected) colorScheme.secondary else cardBorderColor.copy(alpha = 0.3f)

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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = location.icon.icon,
            contentDescription = location.name,
            tint = if (isSelected) colorScheme.secondary else textSecondaryColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = location.name,
            style = ExitSenseTypography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) colorScheme.secondary else textPrimaryColor
        )
    }
}
