package com.rajsv.exitsense.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rajsv.exitsense.data.model.UserDataStore
import com.rajsv.exitsense.ui.screens.additem.AddItemScreen
import com.rajsv.exitsense.ui.screens.addlocation.AddLocationScreen
import com.rajsv.exitsense.ui.screens.auth.LoginScreen
import com.rajsv.exitsense.ui.screens.history.HistoryScreen
import com.rajsv.exitsense.ui.screens.home.HomeScreen
import com.rajsv.exitsense.ui.screens.items.ItemsScreen
import com.rajsv.exitsense.ui.screens.locations.LocationsScreen
import com.rajsv.exitsense.ui.screens.settings.SettingsScreen
import com.rajsv.exitsense.ui.screens.splash.SplashScreen
import com.rajsv.exitsense.ui.screens.stats.StatisticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    val context = LocalContext.current
    val isLoggedIn by UserDataStore.isLoggedIn(context).collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    val loggedIn = isLoggedIn ?: return@SplashScreen
                    val destination = if (loggedIn) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Screen
        composable(
            route = Screen.Login.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    // For now, just stay on login
                    // You can add SignUpScreen later
                }
            )
        }

        // Main Bottom Nav Screens
        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(route = Screen.Items.route) {
            ItemsScreen(navController = navController)
        }

        composable(route = Screen.Locations.route) {
            LocationsScreen(navController = navController)
        }

        composable(route = Screen.History.route) {
            HistoryScreen(navController = navController)
        }

        composable(route = Screen.Statistics.route) {
            StatisticsScreen(navController = navController)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Secondary Screens with slide animation
        composable(
            route = Screen.AddItem.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            AddItemScreen(
                onNavigateBack = { navController.popBackStack() },
                onItemAdded = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddLocation.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            AddLocationScreen(
                onNavigateBack = { navController.popBackStack() },
                onLocationAdded = { navController.popBackStack() }
            )
        }
    }
}