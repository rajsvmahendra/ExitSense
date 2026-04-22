package com.rajsv.exitsense.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rajsv.exitsense.data.model.HistoryEntry
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.StatusError
import com.rajsv.exitsense.ui.theme.StatusSuccess
import com.rajsv.exitsense.ui.theme.StatusWarning

@Composable
fun HistoryCard(
    entry: HistoryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTimelineDot: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "history_card_scale"
    )

    val shape = RoundedCornerShape(20.dp)

    val statusInfo = when (entry.status) {
        HistoryStatus.ACKNOWLEDGED -> StatusInfo(
            label = "Done",
            icon = Icons.Rounded.Check,
            color = ExitSenseTheme.customColors.statusSuccess
        )
        HistoryStatus.REMINDED -> StatusInfo(
            label = "Reminded",
            icon = Icons.Rounded.NotificationsActive,
            color = MaterialTheme.colorScheme.primary
        )
        HistoryStatus.FORGOT -> StatusInfo(
            label = "Forgot",
            icon = Icons.Rounded.Warning,
            color = MaterialTheme.colorScheme.error
        )
        HistoryStatus.DISMISSED -> StatusInfo(
            label = "Skipped",
            icon = Icons.Rounded.Close,
            color = ExitSenseTheme.customColors.statusWarning
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline Dot
        if (showTimelineDot) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusInfo.color)
                )

                // Timeline Line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        // Card Content
        Box(
            modifier = Modifier
                .weight(1f)
                .scale(scale)
                .shadow(
                    elevation = 6.dp,
                    shape = shape,
                    ambientColor = ExitSenseTheme.customColors.shadow,
                    spotColor = ExitSenseTheme.customColors.shadow
                )
                .clip(shape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = 1.dp,
                    color = ExitSenseTheme.customColors.cardBorder,
                    shape = shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item Icon
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusInfo.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = entry.itemIcon.icon,
                            contentDescription = entry.itemName,
                            tint = statusInfo.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Content
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = entry.itemName,
                            style = ExitSenseTypography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (entry.locationName != null) {
                                Icon(
                                    imageVector = Icons.Outlined.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = entry.locationName,
                                    style = ExitSenseTypography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Text(
                                text = entry.time,
                                style = ExitSenseTypography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Badge
                    StatusBadge(
                        status = entry.status,
                        statusInfo = statusInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: HistoryStatus,
    statusInfo: StatusInfo
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusInfo.color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = statusInfo.icon,
            contentDescription = null,
            tint = statusInfo.color,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = statusInfo.label,
            style = ExitSenseTypography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = statusInfo.color
        )
    }
}

private data class StatusInfo(
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun HistoryDateHeader(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )

        Text(
            text = date,
            style = ExitSenseTypography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
    }
}
