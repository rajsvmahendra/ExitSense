package com.rajsv.exitsense.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.rajsv.exitsense.data.model.SettingsDataStore
import com.rajsv.exitsense.data.model.UserDataStore
import com.rajsv.exitsense.data.repository.AuthRepository
import com.rajsv.exitsense.service.NotificationHelper
import com.rajsv.exitsense.ui.components.PremiumButton
import com.rajsv.exitsense.ui.components.PremiumTextField
import com.rajsv.exitsense.ui.components.SecondaryButton
import com.rajsv.exitsense.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Simple manual injection for now (consider Hilt for larger projects)
    val authRepository = remember { AuthRepository(context) }
    val viewModel: LoginViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(authRepository) as T
        }
    })

    val uiState by viewModel.uiState.collectAsState()

    // Theme colors
    val isDarkMode by SettingsDataStore.getDarkMode(context).collectAsState(initial = true)
    val backgroundColor = if (isDarkMode) BackgroundPrimary else LightThemeColors.BackgroundPrimary
    val textPrimaryColor = if (isDarkMode) TextPrimary else LightThemeColors.TextPrimary
    val textSecondaryColor = if (isDarkMode) TextSecondary else LightThemeColors.TextSecondary
    val textTertiaryColor = if (isDarkMode) TextTertiary else LightThemeColors.TextTertiary

    var showContent by remember { mutableStateOf(false) }

    // Google Sign-In Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    Toast.makeText(context, "Google login failed: ID Token is null", Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
            } catch (e: ApiException) {
                val message = when (e.statusCode) {
                    10 -> "Configuration error (Developer Error 10). Check SHA-1 or Web Client ID."
                    12500 -> "Firebase configuration issue (12500). Verify google-services.json."
                    else -> "Google sign in failed: ${e.message}"
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            viewModel.resetState()
        } else {
            Toast.makeText(context, "Sign in cancelled", Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            scope.launch {
                val userName = UserDataStore.getUserName(context).first()
                NotificationHelper.showWelcomeNotification(context, userName)
                onLoginSuccess()
            }
        } else if (uiState is LoginUiState.Error) {
            Toast.makeText(context, (uiState as LoginUiState.Error).message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Section
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(GradientStart, GradientMiddle, GradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = "ExitSense Logo",
                            tint = ExitSenseTheme.customColors.onGradient,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "ExitSense",
                        style = ExitSenseTypography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Google Sign In Button
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 200))
            ) {
                PremiumButton(
                    text = "Continue with Google",
                    onClick = { launcher.launch(viewModel.getGoogleSignInClient().signInIntent) },
                    isLoading = uiState is LoginUiState.Loading,
                    icon = Icons.Outlined.AccountCircle // You can replace with a custom Google G icon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = textTertiaryColor.copy(alpha = 0.2f))
                Text(
                    text = " OR ",
                    style = ExitSenseTypography.bodySmall,
                    color = textTertiaryColor,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = textTertiaryColor.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guest Option
            SecondaryButton(
                text = "Continue as Guest",
                onClick = {
                    scope.launch {
                        UserDataStore.login(context, "Guest", "guest@exitsense.app")
                        NotificationHelper.showWelcomeNotification(context, "Guest")
                        onLoginSuccess()
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                style = ExitSenseTypography.bodySmall,
                color = textTertiaryColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}