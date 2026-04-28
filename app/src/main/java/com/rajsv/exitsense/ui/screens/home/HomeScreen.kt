package com.rajsv.exitsense.ui.screens.home

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.model.ReminderItem
import com.rajsv.exitsense.data.repository.HistoryRepository
import com.rajsv.exitsense.data.repository.ItemsRepository
import com.rajsv.exitsense.navigation.Screen
import com.rajsv.exitsense.service.NotificationHelper
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.components.CompactItemCard
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.ItemCard
import com.rajsv.exitsense.ui.components.PremiumFAB
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showContent by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val allItems by ItemsRepository.getItems(context).collectAsState(initial = emptyList())
    val historyStats by HistoryRepository.getHistoryStats(context).collectAsState(initial = null)

    val todayEssentials = remember(allItems) {
        allItems.filter {
            it.importance == ImportanceLevel.CRITICAL || it.importance == ImportanceLevel.HIGH
        }.take(3)
    }

    LaunchedEffect(key1 = true) {
        delay(150)
        showContent = true
    }

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            PremiumFAB(
                onClick = { navController.navigate(Screen.AddItem.route) },
                icon = Icons.Rounded.Add
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(600, easing = EaseOutQuart)) + 
                                slideInVertically(tween(600, easing = EaseOutQuart)) { -40 }
                    ) {
                        HomeHeader(streak = historyStats?.streak ?: 0)
                    }
                }

                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(700, delayMillis = 100, easing = EaseOutQuart)) +
                                slideInVertically(tween(700, delayMillis = 100, easing = EaseOutQuart)) { 30 }
                    ) {
                        ClassroomDemoControls(
                            onTestReminder = {
                                scope.launch {
                                    NotificationHelper.showReminderNotification(
                                        context,
                                        1002,
                                        "Don't forget! 🔌",
                                        "Did you forget your Charger?",
                                        showToastIfDisabled = true
                                    )
                                }
                            },
                            onSimulateExit = {
                                scope.launch {
                                    val itemsToRemind = if (allItems.isNotEmpty()) {
                                        allItems.take(3).map { it.name }
                                    } else {
                                        listOf("Wallet", "Charger", "ID Card")
                                    }

                                    NotificationHelper.showLocationReminderNotification(
                                        context,
                                        "Home",
                                        itemsToRemind
                                    )

                                    // Add to history for realism
                                    if (allItems.isNotEmpty()) {
                                        val firstItem = allItems.first()
                                        HistoryRepository.addHistoryEntry(
                                            context,
                                            firstItem.id,
                                            firstItem.name,
                                            firstItem.icon,
                                            "Home",
                                            HistoryStatus.REMINDED
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(700, delayMillis = 150, easing = EaseOutQuart)) +
                                slideInVertically(tween(700, delayMillis = 150, easing = EaseOutQuart)) { 30 }
                    ) {
                        SmartSuggestionsCard(
                            items = allItems.take(3)
                        )
                    }
                }

                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(700, delayMillis = 200, easing = EaseOutQuart)) +
                                slideInVertically(tween(700, delayMillis = 200, easing = EaseOutQuart)) { 30 }
                    ) {
                        if (allItems.isEmpty()) {
                            EmptyStateCard(
                                onAddItem = { navController.navigate(Screen.AddItem.route) }
                            )
                        } else if (todayEssentials.isNotEmpty()) {
                            TodayEssentialsCard(
                                items = todayEssentials,
                                onViewAll = { navController.navigate(Screen.Items.route) }
                            )
                        }
                    }
                }

                if (allItems.isNotEmpty()) {
                    item {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(700, delayMillis = 250, easing = EaseOutQuart)) +
                                    slideInVertically(tween(700, delayMillis = 250, easing = EaseOutQuart)) { 30 }
                        ) {
                            SectionHeader(
                                title = "Quick Reminders",
                                onViewAll = { navController.navigate(Screen.Items.route) }
                            )
                        }
                    }

                    item {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(700, delayMillis = 300, easing = EaseOutQuart))
                        ) {
                            QuickRemindersRow(
                                items = allItems.take(5),
                                onItemClick = { /* Navigate to item detail */ }
                            )
                        }
                    }

                    item {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(700, delayMillis = 350, easing = EaseOutQuart)) +
                                    slideInVertically(tween(700, delayMillis = 350, easing = EaseOutQuart)) { 30 }
                        ) {
                            SectionHeader(
                                title = "All Items (${allItems.size})",
                                onViewAll = { navController.navigate(Screen.Items.route) }
                            )
                        }
                    }

                    itemsIndexed(
                        items = allItems.take(4),
                        key = { _, item -> item.id }
                    ) { index, item ->
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showContent,
                            modifier = Modifier.animateItem(),
                            enter = fadeIn(tween(500, delayMillis = 400 + (index * 60), easing = EaseOutQuart)) +
                                    slideInVertically(tween(500, delayMillis = 400 + (index * 60), easing = EaseOutQuart)) { 20 }
                        ) {
                            ItemCard(
                                item = item,
                                onClick = { /* Navigate to item detail */ },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Top fade gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun ClassroomDemoControls(
    onTestReminder: () -> Unit,
    onSimulateExit: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DemoButton(
                text = "Test Reminder",
                icon = Icons.Rounded.NotificationsActive,
                onClick = onTestReminder,
                modifier = Modifier.weight(1f),
                color = GradientStart
            )

            DemoButton(
                text = "Simulate Exit",
                icon = Icons.Rounded.Bolt,
                onClick = onSimulateExit,
                modifier = Modifier.weight(1f),
                color = GradientEnd
            )
        }
    }
}

@Composable
private fun DemoButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text,
                style = ExitSenseTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun SmartSuggestionsCard(
    items: List<ReminderItem>
) {
    val colorScheme = MaterialTheme.colorScheme

    val displayItems = if (items.isEmpty()) {
        listOf("Wallet", "Charger", "ID Card")
    } else {
        items.map { it.name }
    }

    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(
            colorScheme.surfaceVariant.copy(alpha = 0.4f),
            colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        onClick = { },
        cornerRadius = 28.dp,
        contentPadding = 20.dp
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GradientMiddle.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = GradientMiddle,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Today you may need:",
                    style = ExitSenseTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            displayItems.forEach { itemName ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = itemName,
                        style = ExitSenseTypography.bodyLarge,
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(streak: Int) {
    val greeting = getGreeting()
    val currentDate = getCurrentDate()
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = greeting,
                style = ExitSenseTypography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = currentDate,
                style = ExitSenseTypography.labelMedium,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }

        if (streak > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF9800).copy(alpha = 0.12f),
                                Color(0xFFFF5722).copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(
                        1.dp, 
                        Color(0xFFFF9800).copy(alpha = 0.2f), 
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$streak",
                        style = ExitSenseTypography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF9800)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "🔥", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    onAddItem: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val onGradientColor = ExitSenseTheme.customColors.onGradient
    
    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(GradientStart, GradientMiddle, GradientEnd),
        onClick = onAddItem,
        cornerRadius = 32.dp,
        contentPadding = 28.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(onGradientColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = onGradientColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Start Your Journey",
                style = ExitSenseTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onGradientColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Add items to get smart reminders when you leave a place.",
                style = ExitSenseTypography.bodyMedium,
                color = onGradientColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(onGradientColor.copy(alpha = 0.2f))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = onGradientColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add First Item",
                        style = ExitSenseTypography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = onGradientColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayEssentialsCard(
    items: List<ReminderItem>,
    onViewAll: () -> Unit
) {
    val onGradientColor = ExitSenseTheme.customColors.onGradient

    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(GradientStart, GradientMiddle, GradientEnd),
        onClick = onViewAll,
        cornerRadius = 32.dp,
        contentPadding = 24.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = onGradientColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Today's Essentials",
                        style = ExitSenseTypography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onGradientColor
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View All",
                    tint = onGradientColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            items.forEach { item ->
                EssentialItemRow(item = item, onGradientColor = onGradientColor)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun EssentialItemRow(
    item: ReminderItem,
    onGradientColor: Color = ExitSenseTheme.customColors.onGradient
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = onGradientColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(10.dp)
        ) {
            Icon(
                imageVector = item.icon.icon,
                contentDescription = item.name,
                tint = onGradientColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = ExitSenseTypography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = onGradientColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.locationName != null) {
                Text(
                    text = item.locationName,
                    style = ExitSenseTypography.labelSmall,
                    color = onGradientColor.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAll: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colorScheme.primary.copy(alpha = 0.08f))
                .border(1.dp, colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onViewAll
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "View All",
                style = ExitSenseTypography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                letterSpacing = 0.3.sp
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "View All",
                tint = colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QuickRemindersRow(
    items: List<ReminderItem>,
    onItemClick: (ReminderItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            CompactItemCard(
                item = item,
                onClick = { onItemClick(item) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning ☀️"
        hour < 17 -> "Good Afternoon 🌤️"
        hour < 21 -> "Good Evening 🌆"
        else -> "Good Night 🌙"
    }
}

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    return dateFormat.format(Calendar.getInstance().time)
}
