package com.rajsv.exitsense.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Add
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.model.ReminderItem
import com.rajsv.exitsense.data.repository.ItemsRepository
import com.rajsv.exitsense.navigation.Screen
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.components.CompactItemCard
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.ItemCard
import com.rajsv.exitsense.ui.components.PremiumFAB
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    var showContent by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val allItems by ItemsRepository.getItems(context).collectAsState(initial = emptyList())

    val todayEssentials = remember(allItems) {
        allItems.filter {
            it.importance == ImportanceLevel.CRITICAL || it.importance == ImportanceLevel.HIGH
        }.take(3)
    }

    LaunchedEffect(key1 = true) {
        delay(100)
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
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
                    ) {
                        HomeHeader()
                    }
                }

                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(600, delayMillis = 100)) +
                                slideInVertically(tween(600, delayMillis = 100)) { 50 }
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
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 200)) +
                                    slideInVertically(tween(600, delayMillis = 200)) { 50 }
                        ) {
                            SectionHeader(
                                title = "Quick Reminders",
                                onViewAll = { navController.navigate(Screen.Items.route) }
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 250))
                        ) {
                            QuickRemindersRow(
                                items = allItems.take(5),
                                onItemClick = { /* Navigate to item detail */ }
                            )
                        }
                    }

                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(600, delayMillis = 300)) +
                                    slideInVertically(tween(600, delayMillis = 300)) { 50 }
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
                        AnimatedVisibility(
                            visible = showContent,
                            modifier = Modifier.animateItem(),
                            enter = fadeIn(tween(400, delayMillis = 350 + (index * 50))) +
                                    slideInVertically(tween(400, delayMillis = 350 + (index * 50))) { 30 }
                        ) {
                            ItemCard(
                                item = item,
                                onClick = { /* Navigate to item detail */ },
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Top fade gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                colorScheme.background,
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    val greeting = getGreeting()
    val currentDate = getCurrentDate()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 16.dp)
    ) {
        Text(
            text = greeting,
            style = ExitSenseTypography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = currentDate,
            style = ExitSenseTypography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )
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
        cornerRadius = 28.dp,
        contentPadding = 24.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(onGradientColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = onGradientColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Items Yet",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = onGradientColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your first item to get smart reminders before you leave!",
                style = ExitSenseTypography.bodyMedium,
                color = onGradientColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(onGradientColor.copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 18.dp)
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
                        text = "Add Your First Item",
                        style = ExitSenseTypography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
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
        cornerRadius = 28.dp,
        contentPadding = 24.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Essentials",
                    style = ExitSenseTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onGradientColor
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = "View All",
                    tint = onGradientColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            items.forEach { item ->
                EssentialItemRow(item = item, onGradientColor = onGradientColor)
                if (item != items.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                style = ExitSenseTypography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = onGradientColor
            )

            if (item.locationName != null) {
                Text(
                    text = item.locationName,
                    style = ExitSenseTypography.bodySmall,
                    color = onGradientColor.copy(alpha = 0.7f)
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
                .clip(RoundedCornerShape(8.dp))
                .background(colorScheme.primary.copy(alpha = 0.05f))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onViewAll
                )
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "View All",
                style = ExitSenseTypography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colorScheme.primary
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
