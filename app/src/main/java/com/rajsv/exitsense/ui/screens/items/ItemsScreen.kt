package com.rajsv.exitsense.ui.screens.items

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.FabPosition
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.rajsv.exitsense.ui.components.ItemCard
import com.rajsv.exitsense.ui.components.PremiumChip
import com.rajsv.exitsense.ui.components.PremiumFAB
import com.rajsv.exitsense.ui.components.PremiumTextField
import com.rajsv.exitsense.ui.components.SearchBar
import com.rajsv.exitsense.ui.components.SmallFAB
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.AccentSecondary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ItemsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) }

    val filters = listOf("All", "Critical", "High", "Medium", "Low")

    // Get real items from repository
    val allItems by ItemsRepository.getItems(context).collectAsState(initial = emptyList())

    val filteredItems = remember(searchQuery, selectedFilter, allItems) {
        allItems.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    (item.locationName?.contains(searchQuery, ignoreCase = true) ?: false)

            val matchesFilter = when (selectedFilter) {
                0 -> true // All
                1 -> item.importance == ImportanceLevel.CRITICAL
                2 -> item.importance == ImportanceLevel.HIGH
                3 -> item.importance == ImportanceLevel.MEDIUM
                4 -> item.importance == ImportanceLevel.LOW
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedWith(compareByDescending<ReminderItem> { it.isPinned }.thenByDescending { it.createdAt })
    }

    LaunchedEffect(key1 = true) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    ItemsHeader(
                        itemCount = allItems.size
                    )
                }
            }

            // Search Bar
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 100)) +
                            slideInVertically(tween(500, delayMillis = 100)) { 30 }
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search items...",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
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

            // Items Count Info
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 200))
                ) {
                    ItemsCountInfo(
                        totalCount = allItems.size,
                        filteredCount = filteredItems.size,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }

            // Empty State
            if (allItems.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 250))
                    ) {
                        EmptyItemsState(
                            onAddItem = { navController.navigate(Screen.AddItem.route) }
                        )
                    }
                }
            } else if (filteredItems.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 250))
                    ) {
                        NoMatchingItemsState()
                    }
                }
            }

            itemsIndexed(
                items = filteredItems,
                key = { _, item -> item.id }
            ) { index, item ->
                AnimatedVisibility(
                    visible = showContent,
                    modifier = Modifier.animateItem(),
                    enter = fadeIn(tween(400, delayMillis = 250 + (index * 50))) +
                            slideInVertically(tween(400, delayMillis = 250 + (index * 50))) { 30 }
                ) {
                    ItemCard(
                        item = item,
                        onClick = {
                            // Navigate to item detail
                            Toast.makeText(context, "Item: ${item.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            scope.launch {
                                ItemsRepository.deleteItem(context, item.id)
                                Toast.makeText(context, "${item.name} deleted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            // TODO: Navigate to edit screen
                            Toast.makeText(context, "Edit: ${item.name}", Toast.LENGTH_SHORT).show()
                        },
                        onPin = {
                            scope.launch {
                                ItemsRepository.updateItem(context, item.copy(isPinned = !item.isPinned))
                                val message = if (item.isPinned) "${item.name} unpinned" else "${item.name} pinned"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enableSwipe = true,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemsHeader(
    itemCount: Int
) {
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
                    imageVector = Icons.Rounded.Inventory2,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "My Items",
                    style = ExitSenseTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (itemCount == 0) "No items yet" else "$itemCount items tracked",
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        itemsIndexed(filters, key = { _, filter -> filter }) { index, filter ->
            PremiumChip(
                text = filter,
                isSelected = selectedIndex == index,
                onClick = { onFilterSelected(index) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ItemsCountInfo(
    totalCount: Int,
    filteredCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when {
                totalCount == 0 -> "Add your first item"
                filteredCount == totalCount -> "Showing all $totalCount items"
                else -> "Showing $filteredCount of $totalCount items"
            },
            style = ExitSenseTypography.bodySmall,
            color = ExitSenseTheme.customColors.textMuted
        )
    }
}

@Composable
private fun EmptyItemsState(
    onAddItem: () -> Unit
) {
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
                imageVector = Icons.Rounded.Inventory2,
                contentDescription = null,
                tint = AccentSecondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No items yet",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add items you want to be reminded about when leaving a location",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        PremiumChip(
            text = "Add New Item",
            isSelected = true,
            onClick = onAddItem,
            icon = Icons.Rounded.Add
        )
    }
}

@Composable
private fun NoMatchingItemsState() {
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
                .background(AccentPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No matching items",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try adjusting your search or filters",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}