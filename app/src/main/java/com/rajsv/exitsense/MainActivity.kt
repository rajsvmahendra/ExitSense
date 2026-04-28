package com.rajsv.exitsense

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.fragment.app.FragmentActivity
import com.rajsv.exitsense.data.model.SettingsDataStore
import com.rajsv.exitsense.navigation.NavGraph
import com.rajsv.exitsense.service.NotificationHelper
import com.rajsv.exitsense.ui.theme.BackgroundPrimary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.ExitSenseTypography
import com.rajsv.exitsense.ui.theme.GradientEnd
import com.rajsv.exitsense.ui.theme.GradientMiddle
import com.rajsv.exitsense.ui.theme.GradientStart
import com.rajsv.exitsense.ui.theme.LightThemeColors
import com.rajsv.exitsense.ui.theme.TextPrimary

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        enableEdgeToEdge()

        setContent {
            // Get dark mode setting from DataStore
            val isDarkMode by SettingsDataStore.getDarkMode(this).collectAsState(initial = true)
            val appLockEnabled by SettingsDataStore.getAppLock(this).collectAsState(initial = false)
            
            var isUnlocked by remember { mutableStateOf(false) }

            ExitSenseTheme(darkTheme = isDarkMode) {
                val backgroundColor = if (isDarkMode) BackgroundPrimary else LightThemeColors.BackgroundPrimary

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    if (appLockEnabled && !isUnlocked) {
                        LockScreen(
                            onUnlock = {
                                showAppLockPrompt(onResult = { success ->
                                    if (success) isUnlocked = true
                                })
                            },
                            onExit = { finish() }
                        )
                        
                        LaunchedEffect(Unit) {
                            showAppLockPrompt(
                                onResult = { success ->
                                    if (success) isUnlocked = true
                                }
                            )
                        }
                    } else {
                        val navController = rememberNavController()
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }

    private fun showAppLockPrompt(onResult: (Boolean) -> Unit) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(this@MainActivity, errString, Toast.LENGTH_SHORT).show()
                    }
                    onResult(false)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(true)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(false)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("ExitSense Locked")
            .setSubtitle("Authenticate to open the app")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun LockScreen(
    onUnlock: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ExitSense is Locked",
                style = ExitSenseTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please authenticate to continue",
                style = ExitSenseTypography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onUnlock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GradientMiddle
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = "Unlock App",
                    style = ExitSenseTypography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.TextButton(onClick = onExit) {
                Text(
                    text = "Exit App",
                    color = TextPrimary.copy(alpha = 0.5f)
                )
            }
        }
    }
}
