package com.rajsv.exitsense.ui.screens.stats

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.repository.HistoryRepository
import com.rajsv.exitsense.data.repository.ItemsRepository
import com.rajsv.exitsense.data.repository.LocationsRepository
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StatisticsScreen(navController: NavController) {
    val context = LocalContext.current

    val historyEntries by HistoryRepository.getHistoryEntries(context).collectAsState(initial = emptyList())
    val allItems by ItemsRepository.getItems(context).collectAsState(initial = emptyList())
    val allLocations by LocationsRepository.getLocations(context).collectAsState(initial = emptyList())
    val stats by HistoryRepository.getHistoryStats(context).collectAsState(initial = null)

    val topEssentials = remember(allItems) {
        allItems.filter { it.importance == ImportanceLevel.CRITICAL || it.importance == ImportanceLevel.HIGH }.take(3)
    }

    val mostForgotten = remember(historyEntries) {
        historyEntries.filter { it.status == HistoryStatus.FORGOT }
            .groupBy { it.itemName }
            .maxByOrNull { it.value.size }?.key ?: "None"
    }

    val topLocation = remember(historyEntries) {
        val mostUsedInHistory = historyEntries.filter { it.locationName != null }
            .groupBy { it.locationName }
            .maxByOrNull { it.value.size }?.key
        
        mostUsedInHistory ?: allLocations.firstOrNull()?.name ?: "N/A"
    }

    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, easing = EaseOutQuart)) + 
                            slideInVertically(tween(600, easing = EaseOutQuart)) { -40 }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                        Text(
                            text = "Smart Stats",
                            style = ExitSenseTypography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Your activity and habits",
                            style = ExitSenseTypography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            val isAppEmpty = historyEntries.isEmpty() && allItems.isEmpty() && allLocations.isEmpty() && showContent

            if (isAppEmpty) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(700))
                    ) {
                        EmptyStatsState()
                    }
                }
            } else if (showContent) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(700, delayMillis = 100, easing = EaseOutQuart)) + 
                                slideInVertically(tween(700, delayMillis = 100, easing = EaseOutQuart)) { 40 }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            StatCard(
                                title = "Total Success",
                                value = stats?.acknowledged?.toString() ?: "0",
                                icon = Icons.Outlined.Star,
                                gradient = listOf(GradientStart, GradientMiddle),
                                streak = stats?.streak ?: 0
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                StatMiniCard(
                                    title = "Saved Items",
                                    value = allItems.size.toString(),
                                    icon = Icons.Outlined.Inventory2,
                                    modifier = Modifier.weight(1f),
                                    color = AccentPrimary
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                StatMiniCard(
                                    title = "Saved Places",
                                    value = allLocations.size.toString(),
                                    icon = Icons.Outlined.Place,
                                    modifier = Modifier.weight(1f),
                                    color = AccentSecondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth()) {
                                StatMiniCard(
                                    title = "Most Forgotten",
                                    value = mostForgotten,
                                    icon = Icons.Outlined.History,
                                    modifier = Modifier.weight(1.1f),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                StatMiniCard(
                                    title = "Top Place",
                                    value = topLocation,
                                    icon = Icons.Outlined.LocationOn,
                                    modifier = Modifier.weight(0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            SuccessRateCard(
                                rate = stats?.successRate ?: 0
                            )
                        }
                    }
                }

                if (topEssentials.isNotEmpty()) {
                    item {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(700, delayMillis = 200, easing = EaseOutQuart))
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                                Text(
                                    text = "Quick Insights",
                                    style = ExitSenseTypography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                InsightsCard(items = topEssentials)
                            }
                        }
                    }
                }

                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(700, delayMillis = 300, easing = EaseOutQuart)) + 
                                slideInVertically(tween(700, delayMillis = 300, easing = EaseOutQuart)) { 40 }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "Activity Breakdown",
                                style = ExitSenseTypography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            stats?.let { s ->
                                ActivityBreakdown(
                                    acknowledged = s.acknowledged,
                                    forgotten = s.forgotten,
                                    dismissed = s.dismissed,
                                    total = s.totalReminders
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsCard(items: List<com.rajsv.exitsense.data.model.ReminderItem>) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = GradientMiddle,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "High Priority Essentials",
                    style = ExitSenseTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item.name,
                        style = ExitSenseTypography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
@Composable
private fun EmptyStatsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AccentPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Analytics,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No stats yet",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add items or places to start seeing your smart stats here.",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SuccessRateCard(rate: Int) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AccentPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rate%",
                    style = ExitSenseTypography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AccentPrimary
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "Completion Rate",
                    style = ExitSenseTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Reminders successfully acknowledged",
                    style = ExitSenseTypography.labelSmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ActivityBreakdown(
    acknowledged: Int,
    forgotten: Int,
    dismissed: Int,
    total: Int
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(24.dp)
    ) {
        Column {
            BreakdownRow("Acknowledged", acknowledged, total, StatusSuccess, colorScheme.onBackground)
            Spacer(modifier = Modifier.height(18.dp))
            BreakdownRow("Forgotten", forgotten, total, colorScheme.error, colorScheme.onBackground)
            Spacer(modifier = Modifier.height(18.dp))
            BreakdownRow("Dismissed", dismissed, total, ExitSenseTheme.customColors.statusWarning, colorScheme.onBackground)
        }
    }
}

@Composable
fun BreakdownRow(label: String, count: Int, total: Int, color: Color, textColor: Color) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = ExitSenseTypography.bodyMedium, color = textColor)
            Text(text = "$count", style = ExitSenseTypography.bodyMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    streak: Int = 0
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.horizontalGradient(gradient))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    style = ExitSenseTypography.labelMedium, 
                    fontWeight = FontWeight.Bold,
                    color = ExitSenseTheme.customColors.onGradient.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value, 
                    style = ExitSenseTypography.displaySmall, 
                    fontWeight = FontWeight.Black, 
                    color = ExitSenseTheme.customColors.onGradient
                )
                
                if (streak > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ExitSenseTheme.customColors.onGradient.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$streak Day Streak",
                                    style = ExitSenseTypography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ExitSenseTheme.customColors.onGradient
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "🔥", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(ExitSenseTheme.customColors.onGradient.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ExitSenseTheme.customColors.onGradient,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = AccentPrimary
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, colorScheme.outline.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
            .padding(20.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title, 
                style = ExitSenseTypography.labelSmall, 
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value, 
                style = ExitSenseTypography.titleMedium, 
                fontWeight = FontWeight.Bold, 
                color = colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
