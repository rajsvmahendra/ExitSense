package com.rajsv.exitsense.ui.screens.additem

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rajsv.exitsense.data.model.DayOfWeek
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.model.ItemIcon
import com.rajsv.exitsense.data.model.Location
import com.rajsv.exitsense.data.model.ReminderCondition
import com.rajsv.exitsense.data.model.ReminderItem
import com.rajsv.exitsense.data.repository.ItemsRepository
import com.rajsv.exitsense.data.repository.LocationsRepository
import com.rajsv.exitsense.ui.components.CompactLocationCard
import com.rajsv.exitsense.ui.components.DayChip
import com.rajsv.exitsense.ui.components.ImportanceSelector
import com.rajsv.exitsense.ui.components.PremiumButton
import com.rajsv.exitsense.ui.components.PremiumCard
import com.rajsv.exitsense.ui.components.PremiumChip
import com.rajsv.exitsense.ui.components.PremiumTextField
import com.rajsv.exitsense.ui.components.SecondaryButton
import com.rajsv.exitsense.ui.components.SmallFAB
import com.rajsv.exitsense.ui.components.TimePickerDialog
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AddItemScreen(
    onNavigateBack: () -> Unit,
    onItemAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }

    // Form states
    var itemName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(ItemIcon.OTHER) }
    var selectedLocation by remember { mutableStateOf<Location?>(null) }
    var selectedDays = remember { mutableStateListOf<DayOfWeek>() }
    var reminderTime by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf(ReminderCondition.LEAVING_LOCATION) }
    var importanceLevel by remember { mutableIntStateOf(1) }

    var showIconPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Get saved locations from repository
    val savedLocations by LocationsRepository.getLocations(context).collectAsState(initial = emptyList())

    val days = DayOfWeek.entries
    val conditions = ReminderCondition.entries

    LaunchedEffect(key1 = true) {
        delay(100)
        showContent = true
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = 8,
            initialMinute = 30,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { hour, minute, amPm ->
                reminderTime = String.format("%02d:%02d %s", hour, minute, amPm)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddItemTopBar(
                onNavigateBack = onNavigateBack,
                onSave = {
                    if (itemName.isNotBlank() && !isSaving) {
                        isSaving = true
                        scope.launch {
                            val newItem = ReminderItem(
                                id = UUID.randomUUID().toString(),
                                name = itemName.trim(),
                                icon = selectedIcon,
                                locationId = selectedLocation?.id,
                                locationName = selectedLocation?.name,
                                daysNeeded = selectedDays.toList(),
                                reminderTime = reminderTime.takeIf { it.isNotEmpty() },
                                condition = selectedCondition,
                                importance = ImportanceLevel.entries[importanceLevel],
                                isActive = true,
                                createdAt = System.currentTimeMillis()
                            )

                            ItemsRepository.addItem(context, newItem)
                            Toast.makeText(context, "Item '${itemName}' added!", Toast.LENGTH_SHORT).show()
                            onItemAdded()
                        }
                    }
                },
                canSave = itemName.isNotBlank() && !isSaving
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -30 }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Add New Item",
                        style = ExitSenseTypography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Create a reminder for something important",
                        style = ExitSenseTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Item Name Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 100)) +
                        slideInVertically(tween(500, delayMillis = 100)) { 30 }
            ) {
                PremiumTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = "Item Name *",
                    placeholder = "e.g., ID Card, Laptop, Keys...",
                    leadingIcon = Icons.Outlined.Edit,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Icon Selection
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 150))
            ) {
                SectionTitle(title = "Select Icon")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 200))
            ) {
                IconSelector(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it },
                    showAll = showIconPicker,
                    onToggleShowAll = { showIconPicker = !showIconPicker }
                )
            }

            // Location Selection
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 250))
            ) {
                SectionTitle(title = "Link to Location (Optional)")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 300))
            ) {
                if (savedLocations.isEmpty()) {
                    NoLocationsMessage()
                } else {
                    LocationSelector(
                        locations = savedLocations,
                        selectedLocation = selectedLocation,
                        onLocationSelected = { selectedLocation = it }
                    )
                }
            }

            // Days Selection
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                SectionTitle(title = "Days Needed (Optional)")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 400))
            ) {
                DaysSelector(
                    days = days,
                    selectedDays = selectedDays,
                    onDayToggle = { day ->
                        if (selectedDays.contains(day)) {
                            selectedDays.remove(day)
                        } else {
                            selectedDays.add(day)
                        }
                    }
                )
            }

            // Reminder Condition
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 450))
            ) {
                SectionTitle(title = "Remind Me When")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 500))
            ) {
                ConditionSelector(
                    conditions = conditions,
                    selectedCondition = selectedCondition,
                    onConditionSelected = { selectedCondition = it }
                )
            }

            // Reminder Time (if time-based)
            AnimatedVisibility(
                visible = selectedCondition == ReminderCondition.TIME_BASED,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                TimeSelector(
                    selectedTime = reminderTime,
                    onTimeClick = { showTimePicker = true },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Importance Level
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 550))
            ) {
                SectionTitle(title = "Importance Level")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 600))
            ) {
                ImportanceSelector(
                    selectedLevel = importanceLevel,
                    onLevelSelected = { importanceLevel = it },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Save Button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 650))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    PremiumButton(
                        text = if (isSaving) "Saving..." else "Add Item",
                        onClick = {
                            if (itemName.isNotBlank() && !isSaving) {
                                isSaving = true
                                scope.launch {
                                    val newItem = ReminderItem(
                                        id = UUID.randomUUID().toString(),
                                        name = itemName.trim(),
                                        icon = selectedIcon,
                                        locationId = selectedLocation?.id,
                                        locationName = selectedLocation?.name,
                                        daysNeeded = selectedDays.toList(),
                                        reminderTime = reminderTime.takeIf { it.isNotEmpty() },
                                        condition = selectedCondition,
                                        importance = ImportanceLevel.entries[importanceLevel],
                                        isActive = true,
                                        createdAt = System.currentTimeMillis()
                                    )

                                    ItemsRepository.addItem(context, newItem)
                                    Toast.makeText(context, "Item '${itemName}' added!", Toast.LENGTH_SHORT).show()
                                    onItemAdded()
                                }
                            }
                        },
                        enabled = itemName.isNotBlank() && !isSaving,
                        icon = Icons.Rounded.Check,
                        isLoading = isSaving
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SecondaryButton(
                        text = "Cancel",
                        onClick = onNavigateBack,
                        icon = Icons.Outlined.Close
                    )
                }
            }

            // Bottom spacing
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AddItemTopBar(
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallFAB(
            onClick = onNavigateBack,
            icon = Icons.Rounded.ArrowBack,
            backgroundColor = MaterialTheme.colorScheme.surface,
            size = 44.dp,
            iconSize = 20.dp
        )

        if (canSave) {
            SmallFAB(
                onClick = onSave,
                icon = Icons.Rounded.Check,
                backgroundColor = MaterialTheme.colorScheme.primary,
                size = 44.dp,
                iconSize = 20.dp
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = ExitSenseTypography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
    )
}

@Composable
private fun IconSelector(
    selectedIcon: ItemIcon,
    onIconSelected: (ItemIcon) -> Unit,
    showAll: Boolean,
    onToggleShowAll: () -> Unit
) {
    val allIcons = ItemIcon.entries
    val displayedIcons = if (showAll) allIcons else allIcons.take(8)

    val rows = (displayedIcons.size + 3) / 4
    val gridHeight = (rows * 90).dp

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        PremiumCard(
            cornerRadius = 20.dp,
            contentPadding = 16.dp
        ) {
            Column {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false
                ) {
                    items(displayedIcons) { icon ->
                        IconItemWithLabel(
                            icon = icon,
                            isSelected = selectedIcon == icon,
                            onClick = { onIconSelected(icon) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onToggleShowAll)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showAll) "Show Less" else "Show All Icons",
                        style = ExitSenseTypography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (showAll) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IconItemWithLabel(
    icon: ItemIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(150),
        label = "icon_scale"
    )

    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
    val iconColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon.icon,
                contentDescription = icon.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = icon.label,
            style = ExitSenseTypography.labelSmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun NoLocationsMessage() {
    PremiumCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        cornerRadius = 16.dp,
        contentPadding = 16.dp
    ) {
        Text(
            text = "No locations saved yet. Add locations in the Places tab to link items to specific places.",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LocationSelector(
    locations: List<Location>,
    selectedLocation: Location?,
    onLocationSelected: (Location?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            PremiumChip(
                text = "None",
                isSelected = selectedLocation == null,
                onClick = { onLocationSelected(null) },
                icon = Icons.Outlined.Close
            )
        }

        items(locations, key = { it.id }) { location ->
            CompactLocationCard(
                location = location,
                isSelected = selectedLocation?.id == location.id,
                onClick = { onLocationSelected(location) }
            )
        }
    }
}

@Composable
private fun DaysSelector(
    days: List<DayOfWeek>,
    selectedDays: List<DayOfWeek>,
    onDayToggle: (DayOfWeek) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            DayChip(
                day = day.shortName,
                isSelected = selectedDays.contains(day),
                onClick = { onDayToggle(day) }
            )
        }
    }
}

@Composable
private fun ConditionSelector(
    conditions: List<ReminderCondition>,
    selectedCondition: ReminderCondition,
    onConditionSelected: (ReminderCondition) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(conditions) { condition ->
            PremiumChip(
                text = condition.label,
                isSelected = selectedCondition == condition,
                onClick = { onConditionSelected(condition) }
            )
        }
    }
}

@Composable
private fun TimeSelector(
    selectedTime: String,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier,
        onClick = onTimeClick,
        cornerRadius = 16.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reminder Time",
                    style = ExitSenseTypography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (selectedTime.isNotEmpty()) selectedTime else "Tap to select time",
                    style = ExitSenseTypography.bodySmall,
                    color = if (selectedTime.isNotEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (selectedTime.isNotEmpty()) "Change" else "Set",
                    style = ExitSenseTypography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}