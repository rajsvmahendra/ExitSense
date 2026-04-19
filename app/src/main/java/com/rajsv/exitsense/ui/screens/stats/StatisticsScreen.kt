package com.rajsv.exitsense.ui.screens.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Star
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.data.repository.HistoryRepository
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StatisticsScreen(navController: NavController) {
    val context = LocalContext.current

    val historyEntries by HistoryRepository.getHistoryEntries(context).collectAsState(initial = emptyList())
    val stats by HistoryRepository.getHistoryStats(context).collectAsState(initial = null)

    val topItem = remember(historyEntries) {
        historyEntries.groupBy { it.itemName }
            .maxByOrNull { it.value.size }?.key ?: "N/A"
    }

    val topLocation = remember(historyEntries) {
        historyEntries.filter { it.locationName != null }
            .groupBy { it.locationName }
            .maxByOrNull { it.value.size }?.key ?: "N/A"
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
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text(
                    text = "Smart Statistics",
                    style = ExitSenseTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your habits at a glance",
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 50 }
                ) {
                    Column {
                        StatCard(
                            title = "Total Reminders",
                            value = stats?.totalReminders?.toString() ?: "0",
                            icon = Icons.Outlined.NotificationsActive,
                            gradient = listOf(GradientStart, GradientEnd)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            StatMiniCard(
                                title = "Top Item",
                                value = topItem,
                                icon = Icons.Outlined.Inventory2,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            StatMiniCard(
                                title = "Top Place",
                                value = topLocation,
                                icon = Icons.Outlined.LocationOn,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        SuccessRateCard(
                            rate = stats?.successRate ?: 0
                        )
                    }
                }
            }

            item {
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

@Composable
fun SuccessRateCard(rate: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rate%",
                    style = ExitSenseTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentPrimary
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "Success Rate",
                    style = ExitSenseTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Percentage of reminders acknowledged",
                    style = ExitSenseTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        BreakdownRow("Acknowledged", acknowledged, total, StatusSuccess, MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        BreakdownRow("Forgotten", forgotten, total, MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        BreakdownRow("Dismissed", dismissed, total, ExitSenseTheme.customColors.statusWarning, MaterialTheme.colorScheme.onBackground)
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
    gradient: List<Color>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(gradient))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, style = ExitSenseTypography.bodyMedium, color = ExitSenseTheme.customColors.onGradient.copy(alpha = 0.8f))
                Text(text = value, style = ExitSenseTypography.displayMedium, fontWeight = FontWeight.Bold, color = ExitSenseTheme.customColors.onGradient)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ExitSenseTheme.customColors.onGradient.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = ExitSenseTypography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = ExitSenseTypography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
