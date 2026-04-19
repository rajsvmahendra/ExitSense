package com.rajsv.exitsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rajsv.exitsense.data.model.SettingsDataStore
import com.rajsv.exitsense.navigation.NavGraph
import com.rajsv.exitsense.service.NotificationHelper
import com.rajsv.exitsense.ui.theme.BackgroundPrimary
import com.rajsv.exitsense.ui.theme.ExitSenseTheme
import com.rajsv.exitsense.ui.theme.LightThemeColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        enableEdgeToEdge()

        setContent {
            // Get dark mode setting from DataStore
            val isDarkMode by SettingsDataStore.getDarkMode(this).collectAsState(initial = true)

            ExitSenseTheme(darkTheme = isDarkMode) {
                val backgroundColor = if (isDarkMode) BackgroundPrimary else LightThemeColors.BackgroundPrimary

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}