package com.rajsv.exitsense.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.rajsv.exitsense.data.model.SettingsDataStore
import com.rajsv.exitsense.data.model.UserDataStore
import com.rajsv.exitsense.data.repository.AuthRepository
import com.rajsv.exitsense.data.repository.HistoryRepository
import com.rajsv.exitsense.data.repository.ItemsRepository
import com.rajsv.exitsense.data.repository.LocationsRepository
import com.rajsv.exitsense.navigation.Screen
import com.rajsv.exitsense.ui.components.BottomNavBar
import com.rajsv.exitsense.ui.components.GradientCard
import com.rajsv.exitsense.ui.components.PremiumToggle
import com.rajsv.exitsense.ui.theme.AccentPrimary
import com.rajsv.exitsense.ui.theme.AccentSecondary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import com.rajsv.exitsense.ui.theme.StatusError
import com.rajsv.exitsense.ui.theme.StatusWarning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }

    // User data from UserDataStore
    val userName by UserDataStore.getUserName(context).collectAsState(initial = "Guest")
    val userEmail by UserDataStore.getUserEmail(context).collectAsState(initial = "")
    val userPhotoUrl by UserDataStore.getUserPhotoUrl(context).collectAsState(initial = null)

    // Settings states from DataStore
    val isDarkMode by SettingsDataStore.getDarkMode(context).collectAsState(initial = true)
    val notificationsEnabled by SettingsDataStore.getNotifications(context).collectAsState(initial = true)
    val locationEnabled by SettingsDataStore.getLocationServices(context).collectAsState(initial = true)
    val smartReminders by SettingsDataStore.getSmartReminders(context).collectAsState(initial = true)
    val appLockEnabled by SettingsDataStore.getAppLock(context).collectAsState(initial = false)
    val reminderSensitivity by SettingsDataStore.getReminderSensitivity(context).collectAsState(initial = 1)
    val language by SettingsDataStore.getLanguage(context).collectAsState(initial = "English")

    // Permission Launchers
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            if (isGranted) {
                SettingsDataStore.setNotifications(context, true)
                Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
            } else {
                SettingsDataStore.setNotifications(context, false)
                Toast.makeText(context, "Permission Denied: Notifications", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        scope.launch {
            if (fineGranted || coarseGranted) {
                SettingsDataStore.setLocationServices(context, true)
                Toast.makeText(context, "Location Services Enabled", Toast.LENGTH_SHORT).show()
            } else {
                SettingsDataStore.setLocationServices(context, false)
                Toast.makeText(context, "Permission Denied: Location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dialog states
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

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
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, easing = EaseOutQuart)) + 
                            slideInVertically(tween(600, easing = EaseOutQuart)) { -40 }
                ) {
                    SettingsHeader()
                }
            }

            // User Profile Card
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 100, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 100, easing = EaseOutQuart)) { 30 }
                ) {
                    UserProfileCard(
                        userName = userName,
                        userEmail = userEmail,
                        userPhotoUrl = userPhotoUrl
                    )
                }
            }

            // Preferences Section
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 200, easing = EaseOutQuart))
                ) {
                    SectionTitle(title = "Preferences")
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 250, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 250, easing = EaseOutQuart)) { 30 }
                ) {
                    SettingsCard {
                        SettingsToggleItem(
                            icon = Icons.Outlined.DarkMode,
                            title = "Dark Mode",
                            subtitle = if (isDarkMode) "Dark theme active" else "Light theme active",
                            isChecked = isDarkMode,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    SettingsDataStore.setDarkMode(context, enabled)
                                }
                            }
                        )

                        SettingsDivider()

                        SettingsToggleItem(
                            icon = Icons.Outlined.Fingerprint,
                            title = "App Lock",
                            subtitle = if (appLockEnabled) "Biometric security active" else "Protect app with fingerprint",
                            isChecked = appLockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    val biometricManager = BiometricManager.from(context)
                                    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                                        BiometricManager.BIOMETRIC_SUCCESS -> {
                                            showBiometricPrompt(
                                                activity = context as FragmentActivity,
                                                onSuccess = {
                                                    scope.launch {
                                                        SettingsDataStore.setAppLock(context, true)
                                                        Toast.makeText(context, "App Lock Enabled", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                onError = { error ->
                                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                        else -> {
                                            Toast.makeText(context, "Biometrics not available", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                } else {
                                    showBiometricPrompt(
                                        activity = context as FragmentActivity,
                                        onSuccess = {
                                            scope.launch {
                                                SettingsDataStore.setAppLock(context, false)
                                                Toast.makeText(context, "App Lock Disabled", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        )

                        SettingsDivider()

                        SettingsToggleItem(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications",
                            subtitle = if (notificationsEnabled) "Always stay updated" else "Push alerts are disabled",
                            isChecked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        scope.launch {
                                            SettingsDataStore.setNotifications(context, true)
                                            Toast.makeText(context, "Notifications Enabled", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        SettingsDataStore.setNotifications(context, false)
                                        Toast.makeText(context, "Notifications Disabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        SettingsDivider()

                        SettingsToggleItem(
                            icon = Icons.Outlined.LocationOn,
                            title = "Location Services",
                            subtitle = if (locationEnabled) "GPS reminders active" else "Geofencing disabled",
                            isChecked = locationEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    scope.launch {
                                        SettingsDataStore.setLocationServices(context, false)
                                        Toast.makeText(context, "Location disabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Smart Features Section
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 350, easing = EaseOutQuart))
                ) {
                    SectionTitle(title = "Smart Features")
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 400, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 400, easing = EaseOutQuart)) { 30 }
                ) {
                    SettingsCard {
                        SettingsToggleItem(
                            icon = Icons.Outlined.Speed,
                            title = "Smart Reminders",
                            subtitle = if (smartReminders) "Adaptive learning active" else "Basic logic enabled",
                            isChecked = smartReminders,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    SettingsDataStore.setSmartReminders(context, enabled)
                                }
                            }
                        )

                        SettingsDivider()

                        SettingsClickItem(
                            icon = Icons.Outlined.Tune,
                            title = "Reminder Sensitivity",
                            subtitle = getSensitivityText(reminderSensitivity),
                            onClick = { showSensitivityDialog = true }
                        )

                        SettingsDivider()

                        SettingsClickItem(
                            icon = Icons.Outlined.Language,
                            title = "Language",
                            subtitle = language,
                            onClick = { showLanguageDialog = true }
                        )
                    }
                }
            }

            // Data Management Section
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 500, easing = EaseOutQuart))
                ) {
                    SectionTitle(title = "Data Management")
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 550, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 550, easing = EaseOutQuart)) { 30 }
                ) {
                    SettingsCard {
                        SettingsClickItem(
                            icon = Icons.Outlined.History,
                            title = "Clear History",
                            subtitle = "Remove all logs",
                            onClick = { showClearHistoryDialog = true },
                            iconColor = ExitSenseTheme.customColors.statusWarning
                        )

                        SettingsDivider()

                        SettingsClickItem(
                            icon = Icons.Outlined.DeleteOutline,
                            title = "Clear All Data",
                            subtitle = "Factory reset app local storage",
                            onClick = { showClearDataDialog = true },
                            iconColor = MaterialTheme.colorScheme.error,
                            titleColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Support Section
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 650, easing = EaseOutQuart))
                ) {
                    SectionTitle(title = "Community")
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 700, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 700, easing = EaseOutQuart)) { 30 }
                ) {
                    SettingsCard {
                        SettingsClickItem(
                            icon = Icons.Outlined.Star,
                            title = "Rate ExitSense",
                            subtitle = "Your feedback helps us grow",
                            onClick = { showRateDialog = true },
                            iconColor = AccentSecondary
                        )

                        SettingsDivider()

                        SettingsClickItem(
                            icon = Icons.Outlined.Share,
                            title = "Share with Friends",
                            subtitle = "Invite others to use ExitSense",
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "ExitSense - Smart Exit Reminders")
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out ExitSense - the smart reminder app that helps you never forget important items when leaving a place!\n\nDownload now: https://play.google.com/store/apps/details?id=com.rajsv.exitsense"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share ExitSense"))
                            }
                        )
                    }
                }
            }

            // Account Section
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 800, easing = EaseOutQuart))
                ) {
                    SectionTitle(title = "Account")
                }
            }

            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(700, delayMillis = 850, easing = EaseOutQuart)) +
                            slideInVertically(tween(700, delayMillis = 850, easing = EaseOutQuart)) { 30 }
                ) {
                    SettingsCard {
                        SettingsClickItem(
                            icon = Icons.Outlined.ExitToApp,
                            title = "Logout",
                            subtitle = "Currently signed in as $userName",
                            onClick = { showLogoutDialog = true },
                            iconColor = MaterialTheme.colorScheme.error,
                            titleColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // App Info Footer
            item {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 950, easing = EaseOutQuart))
                ) {
                    AppInfoFooter()
                }
            }
        }
    }

    // ==================== DIALOGS ====================

    // Sensitivity Dialog
    if (showSensitivityDialog) {
        SensitivityDialog(
            currentLevel = reminderSensitivity,
            onDismiss = { showSensitivityDialog = false },
            onSelect = { level ->
                scope.launch {
                    SettingsDataStore.setReminderSensitivity(context, level)
                    Toast.makeText(context, "Sensitivity set to ${getSensitivityText(level)}", Toast.LENGTH_SHORT).show()
                }
                showSensitivityDialog = false
            }
        )
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = language,
            onDismiss = { showLanguageDialog = false },
            onSelect = { selectedLanguage ->
                scope.launch {
                    SettingsDataStore.setLanguage(context, selectedLanguage)
                    Toast.makeText(context, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
                }
                showLanguageDialog = false
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        ClearHistoryDialog(
            onDismiss = { showClearHistoryDialog = false },
            onConfirm = {
                scope.launch {
                    HistoryRepository.clearHistory(context)
                    Toast.makeText(context, "History cleared successfully!", Toast.LENGTH_SHORT).show()
                }
                showClearHistoryDialog = false
            }
        )
    }

    // Clear All Data Dialog
    if (showClearDataDialog) {
        ClearDataDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                scope.launch {
                    ItemsRepository.clearAllItems(context)
                    LocationsRepository.clearAllLocations(context)
                    HistoryRepository.clearHistory(context)
                    Toast.makeText(context, "All data cleared successfully!", Toast.LENGTH_SHORT).show()
                }
                showClearDataDialog = false
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                scope.launch {
                    AuthRepository(context).logout()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                showLogoutDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    // Terms of Service Dialog
    if (showTermsDialog) {
        TermsOfServiceDialog(
            onDismiss = { showTermsDialog = false }
        )
    }

    // Rate App Dialog
    if (showRateDialog) {
        RateAppDialog(
            onDismiss = { showRateDialog = false },
            onRate = { rating ->
                Toast.makeText(context, "Thank you for rating us $rating stars!", Toast.LENGTH_SHORT).show()
                showRateDialog = false
            }
        )
    }
}

// ==================== COMPOSABLE COMPONENTS ====================

@Composable
private fun SettingsHeader() {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AccentPrimary.copy(alpha = 0.12f))
                    .border(1.dp, AccentPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = "Settings",
                    style = ExitSenseTypography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground
                )

                Text(
                    text = "Manage your preferences",
                    style = ExitSenseTypography.labelMedium,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun UserProfileCard(
    userName: String,
    userEmail: String,
    userPhotoUrl: String? = null
) {
    val onGradientColor = ExitSenseTheme.customColors.onGradient
    GradientCard(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        gradientColors = listOf(GradientStart, GradientMiddle, GradientEnd),
        cornerRadius = 32.dp,
        contentPadding = 24.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(onGradientColor.copy(alpha = 0.15f))
                    .border(1.5.dp, onGradientColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!userPhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val initial = if (userName.isNotEmpty() && userName != "Guest") userName.first().uppercase() else "G"
                    Text(
                        text = initial,
                        style = ExitSenseTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = onGradientColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (userName.isNotEmpty()) userName else "Guest User",
                    style = ExitSenseTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = onGradientColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (userEmail.isNotEmpty() && userEmail != "guest@exitsense.app") userEmail else "Standard Account",
                    style = ExitSenseTypography.bodyMedium,
                    color = onGradientColor.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(onGradientColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = onGradientColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = ExitSenseTypography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconColor: Color = AccentPrimary
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = ExitSenseTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = ExitSenseTypography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        PremiumToggle(
            isChecked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = AccentPrimary,
    titleColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = ExitSenseTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = titleColor ?: colorScheme.onSurface
            )

            Text(
                text = subtitle,
                style = ExitSenseTypography.labelSmall,
                color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
private fun AppInfoFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ExitSense",
            style = ExitSenseTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Version 1.0.0",
            style = ExitSenseTypography.bodySmall,
            color = ExitSenseTheme.customColors.textMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Made with ❤️ by Rajsv",
            style = ExitSenseTypography.bodySmall,
            color = ExitSenseTheme.customColors.textMuted
        )
    }
}

// ==================== DIALOG COMPOSABLES ====================

@Composable
private fun SensitivityDialog(
    currentLevel: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val levels = listOf("Low", "Medium", "High")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Reminder Sensitivity",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                levels.forEachIndexed { index, level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (currentLevel == index) AccentPrimary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(index) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = level,
                            style = ExitSenseTypography.bodyLarge,
                            fontWeight = if (currentLevel == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentLevel == index) AccentPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLanguage: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val languages = listOf("English", "Hindi", "Spanish", "French", "German")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Select Language",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (currentLanguage == language) AccentPrimary.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .clickable { onSelect(language) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = language,
                            style = ExitSenseTypography.bodyLarge,
                            fontWeight = if (currentLanguage == language) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentLanguage == language) AccentPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ClearHistoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Clear History?",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "This will delete all your reminder history. Your items and locations will not be affected.",
                style = ExitSenseTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear History", color = ExitSenseTheme.customColors.statusWarning, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ClearDataDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Clear All Data?",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "This will permanently delete all your items, locations, and history. This action cannot be undone.",
                style = ExitSenseTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Logout?",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Are you sure you want to logout? Your data will be saved and you can login again anytime.",
                style = ExitSenseTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "About ExitSense",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "Version 1.0.0",
                    style = ExitSenseTypography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ExitSense is a smart reminder app that helps you never forget important items when leaving a location.\n\n" +
                            "Features:\n" +
                            "• Location-based reminders\n" +
                            "• Time-based reminders\n" +
                            "• Smart learning from your habits\n" +
                            "• Beautiful dark & light themes\n" +
                            "• Easy item management\n\n" +
                            "Built with ❤️ using Jetpack Compose for Android.",
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "© 2025 Rajsv. All rights reserved.",
                    style = ExitSenseTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPrimary)
            }
        }
    )
}

@Composable
private fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Privacy Policy",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Last updated: January 2025\n\n" +
                            "1. Information We Collect\n" +
                            "ExitSense collects and stores the following information locally on your device:\n" +
                            "• Items you create for reminders\n" +
                            "• Locations you save\n" +
                            "• App preferences and settings\n" +
                            "• Location data (only when you grant permission)\n\n" +
                            "2. How We Use Your Information\n" +
                            "All data is stored locally on your device and is used solely to provide reminder functionality. We do not:\n" +
                            "• Collect personal information\n" +
                            "• Share data with third parties\n" +
                            "• Upload data to servers\n" +
                            "• Track your activity\n\n" +
                            "3. Location Data\n" +
                            "Location data is used only to trigger reminders when you leave saved locations. This data is processed locally and never leaves your device.\n\n" +
                            "4. Data Security\n" +
                            "Your data is stored securely on your device using Android's DataStore. We recommend keeping your device secure with a PIN or biometric lock.\n\n" +
                            "5. Your Rights\n" +
                            "You can delete all your data at any time from Settings > Clear All Data.\n\n" +
                            "6. Contact Us\n" +
                            "If you have questions about this privacy policy, please contact us at rajasvmahendra@gmail.com",
                    style = ExitSenseTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPrimary)
            }
        }
    )
}

@Composable
private fun TermsOfServiceDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Terms of Service",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Last updated: January 2025\n\n" +
                            "1. Acceptance of Terms\n" +
                            "By using ExitSense, you agree to these terms. If you do not agree, please do not use the app.\n\n" +
                            "2. Use of the App\n" +
                            "ExitSense is provided for personal, non-commercial use. You may:\n" +
                            "• Create and manage reminder items\n" +
                            "• Save locations for reminders\n" +
                            "• Customize app settings\n\n" +
                            "3. User Responsibilities\n" +
                            "You are responsible for:\n" +
                            "• Keeping your device secure\n" +
                            "• The accuracy of information you enter\n" +
                            "• Granting necessary permissions for app functionality\n\n" +
                            "4. Disclaimer\n" +
                            "ExitSense is provided \"as is\" without warranties. We are not responsible for:\n" +
                            "• Missed reminders due to device issues\n" +
                            "• Location accuracy problems\n" +
                            "• Data loss due to device failure\n\n" +
                            "5. Limitation of Liability\n" +
                            "We shall not be liable for any damages arising from the use of this app.\n\n" +
                            "6. Changes to Terms\n" +
                            "We may update these terms at any time. Continued use of the app constitutes acceptance of new terms.\n\n" +
                            "7. Contact\n" +
                            "For questions, contact us at rajasvmahendra@gmail.com",
                    style = ExitSenseTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = AccentPrimary)
            }
        }
    )
}

@Composable
private fun RateAppDialog(
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit
) {
    var selectedRating by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "Rate ExitSense",
                style = ExitSenseTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "How would you rate your experience?",
                    style = ExitSenseTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Star Rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= selectedRating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "Star $star",
                            tint = if (star <= selectedRating) AccentSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { selectedRating = star }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (selectedRating) {
                        1 -> "Poor 😞"
                        2 -> "Fair 😐"
                        3 -> "Good 🙂"
                        4 -> "Great 😊"
                        5 -> "Amazing! 🤩"
                        else -> "Tap to rate"
                    },
                    style = ExitSenseTypography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedRating > 0) AccentPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (selectedRating > 0) onRate(selectedRating) },
                enabled = selectedRating > 0
            ) {
                Text(
                    "Submit",
                    color = if (selectedRating > 0) AccentPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// ==================== HELPER FUNCTIONS ====================

private fun showBiometricPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Authentication failed")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Confirm your identity to change App Lock")
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

private fun getSensitivityText(level: Int): String {
    return when (level) {
        0 -> "Low - Fewer reminders"
        1 -> "Medium - Balanced"
        2 -> "High - More frequent"
        else -> "Medium - Balanced"
    }
}