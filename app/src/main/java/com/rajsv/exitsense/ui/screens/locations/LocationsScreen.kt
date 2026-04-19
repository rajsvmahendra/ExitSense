package com.rajsv.exitsense.ui.screens.locations

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.rajsv.exitsense.data.repository.LocationsRepository
import com.rajsv.exitsense.navigation.Screen
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.LocationCard
import com.rajsv.exitsense.ui.components.PremiumChip
import com.rajsv.exitsense.ui.components.PremiumFAB
import com.rajsv.exitsense.ui.components.PremiumTextField
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.AccentSecondary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import com.rajsv.exitsense.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LocationsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // GPS states
    var isDetectingLocation by remember { mutableStateOf(false) }
    var currentLocationText by remember { mutableStateOf<String?>(null) }

    // Get real locations from repository
    val locations by LocationsRepository.getLocations(context).collectAsState(initial = emptyList())
    val filteredLocations = remember(searchQuery, locations) {
        locations.filter { location ->
            location.name.contains(searchQuery, ignoreCase = true) ||
                    location.address.contains(searchQuery, ignoreCase = true)
        }.sortedWith(compareByDescending<com.rajsv.exitsense.data.model.Location> { it.isPinned }.thenByDescending { it.createdAt })
    }
    val activeLocationsCount = locations.count { it.isActive }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            detectCurrentLocation(
                context = context,
                onLocationDetected = { lat, lng, address ->
                    isDetectingLocation = false
                    currentLocationText = address
                    Toast.makeText(context, "Location: $address", Toast.LENGTH_LONG).show()
                },
                onError = { error ->
                    isDetectingLocation = false
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            isDetectingLocation = false
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
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
            detectCurrentLocation(
                context = context,
                onLocationDetected = { lat, lng, address ->
                    isDetectingLocation = false
                    currentLocationText = address
                    Toast.makeText(context, "Location: $address", Toast.LENGTH_LONG).show()
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
                onClick = { navController.navigate(Screen.AddLocation.route) },
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
                    LocationsHeader(
                        totalCount = locations.size,
                        activeCount = activeLocationsCount
                    )
                }
            }

            // Search Bar
            if (locations.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 50)) +
                                slideInVertically(tween(500, delayMillis = 50)) { 30 }
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            PremiumTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = "Search places...",
                                leadingIcon = Icons.Outlined.Search,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Current Location Card
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 100)) +
                            slideInVertically(tween(600, delayMillis = 100)) { 50 }
                ) {
                    CurrentLocationCard(
                        isDetecting = isDetectingLocation,
                        currentLocationText = currentLocationText,
                        onDetectLocation = { handleDetectLocation() }
                    )
                }
            }

            // Section Header
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(500, delayMillis = 150))
                ) {
                    Text(
                        text = when {
                            locations.isEmpty() -> "No Saved Places"
                            searchQuery.isNotEmpty() -> "Search Results (${filteredLocations.size})"
                            else -> "Saved Places (${locations.size})"
                        },
                        style = ExitSenseTypography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                }
            }

            // Empty State
            if (locations.isEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 200))
                    ) {
                        EmptyLocationsState(
                            onAddLocation = { navController.navigate(Screen.AddLocation.route) }
                        )
                    }
                }
            } else if (filteredLocations.isEmpty() && searchQuery.isNotEmpty()) {
                item {
                    NoMatchingLocationsState()
                }
            }

            // Locations List with Swipe Delete
            itemsIndexed(
                items = filteredLocations,
                key = { _, location -> location.id }
            ) { index, location ->
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400, delayMillis = 200 + (index * 50))) +
                            slideInVertically(tween(400, delayMillis = 200 + (index * 50))) { 30 }
                ) {
                    LocationCard(
                        location = location,
                        onClick = {
                            // Navigate to location detail
                            Toast.makeText(context, "Location: ${location.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            scope.launch {
                                LocationsRepository.deleteLocation(context, location.id)
                                Toast.makeText(context, "${location.name} deleted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            // TODO: Navigate to edit screen
                            Toast.makeText(context, "Edit: ${location.name}", Toast.LENGTH_SHORT).show()
                        },
                        onPin = {
                            scope.launch {
                                LocationsRepository.updateLocation(context, location.copy(isPinned = !location.isPinned))
                                val message = if (location.isPinned) "${location.name} unpinned" else "${location.name} pinned"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                        enableSwipe = true,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }

            // Info Section (only show if locations exist)
            if (locations.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(500, delayMillis = 400))
                    ) {
                        LocationsInfoSection()
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationsHeader(
    totalCount: Int,
    activeCount: Int
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
                    .background(AccentSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Place,
                    contentDescription = null,
                    tint = AccentSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "My Places",
                    style = ExitSenseTypography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = when {
                        totalCount == 0 -> "No places saved yet"
                        else -> "$activeCount of $totalCount places active"
                    },
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrentLocationCard(
    isDetecting: Boolean,
    currentLocationText: String?,
    onDetectLocation: () -> Unit
) {
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
                    .background(TextPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isDetecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.MyLocation,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Current Location",
                    style = ExitSenseTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = when {
                        isDetecting -> "Detecting your position..."
                        currentLocationText != null -> currentLocationText
                        else -> "Tap to detect your location"
                    },
                    style = ExitSenseTypography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }

            if (!isDetecting) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TextPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (currentLocationText != null) "Refresh" else "Detect",
                        style = ExitSenseTypography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLocationsState(
    onAddLocation: () -> Unit
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
                imageVector = Icons.Rounded.Place,
                contentDescription = null,
                tint = AccentSecondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No places saved",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Add places like Home, Office, or Gym to get smart reminders when you leave",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        PremiumChip(
            text = "Add New Place",
            isSelected = true,
            onClick = onAddLocation,
            icon = Icons.Rounded.Add
        )
    }
}

@Composable
private fun NoMatchingLocationsState() {
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
            text = "No matching places",
            style = ExitSenseTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try adjusting your search query",
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LocationsInfoSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "How it works",
            style = ExitSenseTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoItem(
            number = "1",
            text = "Add your frequently visited places"
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoItem(
            number = "2",
            text = "Link items to specific locations"
        )

        Spacer(modifier = Modifier.height(8.dp))

        InfoItem(
            number = "3",
            text = "Get reminded when leaving those places"
        )
    }
}

@Composable
private fun InfoItem(
    number: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = ExitSenseTypography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AccentPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = ExitSenseTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0) ?: "Unknown address"
                } else {
                    "Lat: %.4f, Lng: %.4f".format(latitude, longitude)
                }
                onLocationDetected(latitude, longitude, address)
            } catch (e: Exception) {
                onLocationDetected(latitude, longitude, "Lat: %.4f, Lng: %.4f".format(latitude, longitude))
            }
        } else {
            onError("Could not get location. Try again.")
        }
    }.addOnFailureListener { exception ->
        onError("Location error: ${exception.message}")
    }
}