package com.rajsv.exitsense.ui.screens.addlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rajsv.exitsense.data.model.Location
import com.rajsv.exitsense.data.model.LocationIcon
import com.rajsv.exitsense.data.repository.LocationsRepository
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.PremiumButton
import com.rajsv.exitsense.ui.components.PremiumCard
import com.rajsv.exitsense.ui.components.PremiumTextField
import com.rajsv.exitsense.ui.components.PremiumToggle
import com.rajsv.exitsense.ui.components.SecondaryButton
import com.rajsv.exitsense.ui.components.SmallFAB
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@Composable
fun AddLocationScreen(
    onNavigateBack: () -> Unit,
    onLocationAdded: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }

    // Form states
    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(LocationIcon.OTHER) }
    var radiusInMeters by remember { mutableFloatStateOf(100f) }
    var isActive by remember { mutableStateOf(true) }

    // GPS states
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var isDetectingLocation by remember { mutableStateOf(false) }
    var locationDetected by remember { mutableStateOf(false) }

    // UI states
    var showIconPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Permission launchers
    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Background location recommended for better reminders.", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            detectCurrentLocation(
                context = context,
                onLocationDetected = { lat, lng, address ->
                    latitude = lat
                    longitude = lng
                    locationAddress = address
                    isDetectingLocation = false
                    locationDetected = true
                    Toast.makeText(context, "Location detected!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isDetectingLocation = false
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            isDetectingLocation = false
            Toast.makeText(context, "Location permission denied. Enter address manually.", Toast.LENGTH_LONG).show()
        }
    }

    // Function to handle location detection
    fun handleDetectLocation() {
        isDetectingLocation = true

        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED) {
                    backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            detectCurrentLocation(
                context = context,
                onLocationDetected = { lat, lng, address ->
                    latitude = lat
                    longitude = lng
                    locationAddress = address
                    isDetectingLocation = false
                    locationDetected = true
                    Toast.makeText(context, "Location detected!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    isDetectingLocation = false
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Function to save location
    fun saveLocation() {
        if (locationName.isNotBlank() && !isSaving) {
            isSaving = true
            scope.launch {
                val newLocation = Location(
                    id = UUID.randomUUID().toString(),
                    name = locationName.trim(),
                    icon = selectedIcon,
                    address = locationAddress,
                    latitude = latitude ?: 0.0,
                    longitude = longitude ?: 0.0,
                    radiusInMeters = radiusInMeters.toInt(),
                    isActive = isActive,
                    createdAt = System.currentTimeMillis()
                )

                LocationsRepository.addLocation(context, newLocation)
                Toast.makeText(context, "Location '${locationName}' saved!", Toast.LENGTH_SHORT).show()
                onLocationAdded()
            }
        }
    }

    LaunchedEffect(key1 = true) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddLocationTopBar(
                onNavigateBack = onNavigateBack,
                onSave = { saveLocation() },
                canSave = locationName.isNotBlank() && !isSaving
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
                        text = "Add New Place",
                        style = ExitSenseTypography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Save a location for smart reminders",
                        style = ExitSenseTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Current Location Card
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 100)) +
                        slideInVertically(tween(500, delayMillis = 100)) { 30 }
            ) {
                CurrentLocationCard(
                    isDetecting = isDetectingLocation,
                    locationDetected = locationDetected,
                    onDetectLocation = { handleDetectLocation() }
                )
            }

            // Coordinates Display (if detected)
            AnimatedVisibility(
                visible = locationDetected && latitude != null && longitude != null,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
            ) {
                CoordinatesCard(
                    latitude = latitude ?: 0.0,
                    longitude = longitude ?: 0.0
                )
            }

            // Location Name Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 150)) +
                        slideInVertically(tween(500, delayMillis = 150)) { 30 }
            ) {
                PremiumTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = "Place Name *",
                    placeholder = "e.g., Home, Office, Gym...",
                    leadingIcon = Icons.Outlined.Edit,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Address Field
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 200)) +
                        slideInVertically(tween(500, delayMillis = 200)) { 30 }
            ) {
                PremiumTextField(
                    value = locationAddress,
                    onValueChange = { locationAddress = it },
                    label = "Address (Optional)",
                    placeholder = "Enter address or detect location",
                    leadingIcon = Icons.Outlined.LocationOn,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // Manual Coordinates Entry
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 250))
            ) {
                SectionTitle(title = "Manual Coordinates (Optional)")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 300))
            ) {
                ManualCoordinatesEntry(
                    latitude = latitude,
                    longitude = longitude,
                    onLatitudeChange = {
                        latitude = it
                        if (it != null) locationDetected = true
                    },
                    onLongitudeChange = {
                        longitude = it
                        if (it != null) locationDetected = true
                    }
                )
            }

            // Icon Selection
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 350))
            ) {
                SectionTitle(title = "Select Icon")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 400))
            ) {
                LocationIconSelector(
                    selectedIcon = selectedIcon,
                    onIconSelected = { selectedIcon = it },
                    showAll = showIconPicker,
                    onToggleShowAll = { showIconPicker = !showIconPicker }
                )
            }

            // Radius Slider
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 450))
            ) {
                SectionTitle(title = "Detection Radius")
            }

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 500))
            ) {
                RadiusSelector(
                    radius = radiusInMeters,
                    onRadiusChange = { radiusInMeters = it }
                )
            }

            // Active Toggle
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 550))
            ) {
                ActiveToggleCard(
                    isActive = isActive,
                    onToggle = { isActive = it }
                )
            }

            // Save Button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 600))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    PremiumButton(
                        text = if (isSaving) "Saving..." else "Save Place",
                        onClick = { saveLocation() },
                        enabled = locationName.isNotBlank() && !isSaving,
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
private fun AddLocationTopBar(
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
private fun CurrentLocationCard(
    isDetecting: Boolean,
    locationDetected: Boolean,
    onDetectLocation: () -> Unit
) {
    val onGradientColor = ExitSenseTheme.customColors.onGradient

    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(GradientStart, GradientMiddle, GradientEnd),
        onClick = if (!isDetecting) onDetectLocation else null,
        cornerRadius = 24.dp,
        contentPadding = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(onGradientColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isDetecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = onGradientColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (locationDetected) Icons.Outlined.GpsFixed else Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = onGradientColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (locationDetected) "Location Detected" else "Use Current Location",
                    style = ExitSenseTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onGradientColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = when {
                        isDetecting -> "Detecting your position..."
                        locationDetected -> "GPS coordinates captured"
                        else -> "Auto-detect your current position"
                    },
                    style = ExitSenseTypography.bodySmall,
                    color = onGradientColor.copy(alpha = 0.7f)
                )
            }

            if (!isDetecting) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(onGradientColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (locationDetected) "Update" else "Detect",
                        style = ExitSenseTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = onGradientColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CoordinatesCard(
    latitude: Double,
    longitude: Double
) {
    PremiumCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        cornerRadius = 16.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Latitude",
                    style = ExitSenseTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "%.6f", latitude),
                    style = ExitSenseTypography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Longitude",
                    style = ExitSenseTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "%.6f", longitude),
                    style = ExitSenseTypography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ManualCoordinatesEntry(
    latitude: Double?,
    longitude: Double?,
    onLatitudeChange: (Double?) -> Unit,
    onLongitudeChange: (Double?) -> Unit
) {
    var latText by remember(latitude) { mutableStateOf(latitude?.toString() ?: "") }
    var lngText by remember(longitude) { mutableStateOf(longitude?.toString() ?: "") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumTextField(
            value = latText,
            onValueChange = {
                latText = it
                onLatitudeChange(it.toDoubleOrNull())
            },
            label = "Latitude",
            placeholder = "e.g., 28.6139",
            modifier = Modifier.weight(1f)
        )

        PremiumTextField(
            value = lngText,
            onValueChange = {
                lngText = it
                onLongitudeChange(it.toDoubleOrNull())
            },
            label = "Longitude",
            placeholder = "e.g., 77.2090",
            modifier = Modifier.weight(1f)
        )
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
private fun LocationIconSelector(
    selectedIcon: LocationIcon,
    onIconSelected: (LocationIcon) -> Unit,
    showAll: Boolean,
    onToggleShowAll: () -> Unit
) {
    val allIcons = LocationIcon.entries
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
                        LocationIconItemWithLabel(
                            icon = icon,
                            isSelected = selectedIcon == icon,
                            onClick = { onIconSelected(icon) },
                            modifier = Modifier.animateItem()
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
private fun LocationIconItemWithLabel(
    icon: LocationIcon,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(150),
        label = "icon_scale"
    )

    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer
    val iconColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(
        modifier = modifier
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
private fun RadiusSelector(
    radius: Float,
    onRadiusChange: (Float) -> Unit
) {
    PremiumCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        cornerRadius = 20.dp,
        contentPadding = 20.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RadioButtonChecked,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Detection Radius",
                            style = ExitSenseTypography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Distance to trigger reminders",
                            style = ExitSenseTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${radius.toInt()}m",
                        style = ExitSenseTypography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = radius,
                onValueChange = onRadiusChange,
                valueRange = 25f..500f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "25m",
                    style = ExitSenseTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Text(
                    text = "500m",
                    style = ExitSenseTypography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ActiveToggleCard(
    isActive: Boolean,
    onToggle: (Boolean) -> Unit
) {
    PremiumCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        cornerRadius = 20.dp,
        contentPadding = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
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
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Active Location",
                        style = ExitSenseTypography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = if (isActive) "Reminders enabled" else "Reminders disabled",
                        style = ExitSenseTypography.bodySmall,
                        color = if (isActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            PremiumToggle(
                isChecked = isActive,
                onCheckedChange = onToggle
            )
        }
    }
}

@SuppressLint("MissingPermission")
private fun detectCurrentLocation(
    context: Context,
    onLocationDetected: (Double, Double, String) -> Unit,
    onError: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location: android.location.Location? ->
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude

            // Try to get address from coordinates
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Unknown address"
                } else {
                    "Lat: $latitude, Lng: $longitude"
                }
                onLocationDetected(latitude, longitude, address)
            } catch (e: Exception) {
                onLocationDetected(latitude, longitude, "Lat: $latitude, Lng: $longitude")
            }
        } else {
            onError("Could not get location. Try again or enter manually.")
        }
    }.addOnFailureListener { exception ->
        onError("Location error: ${exception.message}")
    }
}