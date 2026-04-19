package com.rajsv.exitsense.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.data.repository.HistoryRepository
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.HistoryCard
import com.rajsv.exitsense.ui.components.HistoryDateHeader
import com.rajsv.exitsense.ui.components.PremiumChip
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.AccentSecondary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import com.rajsv.exitsense.ui.theme.StatusError
import com.rajsv.exitsense.ui.theme.StatusSuccess
import com.rajsv.exitsense.ui.theme.StatusWarning
import com.rajsv.exitsense.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun HistoryScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var showContent by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filters = listOf("All", "Done", "Forgot", "Reminded", "Skipped")

    // Real data from repository
    val allEntries by HistoryRepository.getHistoryEntries(context).collectAsState(initial = emptyList())
    val stats by HistoryRepository.getHistoryStats(context).collectAsState(
        initial = com.rajsv.exitsense.data.repository.HistoryStats(0, 0, 0, 0)
    )

    val filteredEntries = remember(selectedFilter, allEntries) {
        when (selectedFilter) {
            0 -> allEntries
            1 -> allEntries.filter { it.status == HistoryStatus.ACKNOWLEDGED }
            2 -> allEntries.filter { it.status == HistoryStatus.FORGOT }
            3 -> allEntries.filter { it.status == HistoryStatus.REMINDED }
            4 -> allEntries.filter { it.status == HistoryStatus.DISMISSED }
            else -> allEntries
        }
    }

    // Group entries by date
    val groupedEntries = remember(filteredEntries) {
        filteredEntries.groupBy { it.date }
    }

    LaunchedEffect(key1 = true) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
                ) {
                    HistoryHeader()
                }
            }

            // Stats Card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 100)) +
                            slideInVertically(tween(600, delayMillis = 100)) { 50 }
                ) {
                    StatsCard(
                        total = stats.totalReminders,
                        acknowledged = stats.acknowledged,
                        forgot = stats.forgotten,
                        reminded = stats.totalReminders - stats.acknowledged - stats.forgotten - stats.dismissed,
                        dismissed = stats.dismissed
                    )
                }
            }

            // Filter Chips
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 150))
                ) {
                    FilterChipsRow(
                        filters = filters,
                        selectedIndex = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }
            }

            // Empty State
            if (filteredEntries.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 200))
                    ) {
                        EmptyHistoryState()
                    }
                }
            }

            // Grouped History Entries
            groupedEntries.forEach { (date, entries) ->
                // Date Header
                item(key = "header_$date") {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, delayMillis = 200))
                    ) {
                        HistoryDateHeader(
                            date = date,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                // Entries for this date
                itemsIndexed(
                    items = entries,
                    key = { _, entry -> entry.id }
                ) { index, entry ->
                    val isLastInGroup = index == entries.lastIndex

                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, delayMillis = 250 + (index * 50))) +
                                slideInVertically(tween(400, delayMillis = 250 + (index * 50))) { 30 }
                    ) {
                        HistoryCard(
                            entry = entry,
                            onClick = { /* Show entry detail */ },
                            showTimelineDot = true,
                            modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp,
                                bottom = if (isLastInGroup) 16.dp else 0.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "History",
                    style = ExitSenseTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Your reminder activity",
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    total: Int,
    acknowledged: Int,
    forgot: Int,
    reminded: Int,
    dismissed: Int
) {
    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(GradientStart, GradientMiddle, GradientEnd),
        cornerRadius = 24.dp,
        contentPadding = 20.dp
    ) {
        Column {
            Text(
                text = "All Time Stats",
                style = ExitSenseTypography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Rounded.Check,
                    value = acknowledged.toString(),
                    label = "Done",
                    color = StatusSuccess
                )

                StatItem(
                    icon = Icons.Rounded.Warning,
                    value = forgot.toString(),
                    label = "Forgot",
                    color = MaterialTheme.colorScheme.error
                )

                StatItem(
                    icon = Icons.Rounded.NotificationsActive,
                    value = reminded.toString(),
                    label = "Reminded",
                    color = AccentPrimary
                )

                StatItem(
                    icon = Icons.Rounded.Close,
                    value = dismissed.toString(),
                    label = "Skipped",
                    color = ExitSenseTheme.customColors.statusWarning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Success Rate
            val successRate = if (total > 0) {
                ((acknowledged.toFloat() / total) * 100).toInt()
            } else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Success Rate",
                    style = ExitSenseTypography.bodyMedium,
                    color = TextPrimary.copy(alpha = 0.8f)
                )

                Text(
                    text = "$successRate%",
                    style = ExitSenseTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TextPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = ExitSenseTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            text = label,
            style = ExitSenseTypography.labelSmall,
            color = TextPrimary.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun FilterChipsRow(
    filters: List<String>,
    selectedIndex: Int,
    onFilterSelected: (Int) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        itemsIndexed(filters) { index, filter ->
            PremiumChip(
                text = filter,
                isSelected = selectedIndex == index,
                onClick = { onFilterSelected(index) }
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AccentSecondary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = AccentSecondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No history yet",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your reminder history will appear here",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
