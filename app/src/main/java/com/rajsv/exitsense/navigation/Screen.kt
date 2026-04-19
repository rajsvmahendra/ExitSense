package com.rajsv.exitsense.navigation

sealed class Screen(val route: String) {

    // Auth Screens
    object Login : Screen("login")
    object SignUp : Screen("signup")

    // Splash Screen
    object Splash : Screen("splash")

    // Main Screens (Bottom Navigation)
    object Home : Screen("home")
    object Items : Screen("items")
    object Locations : Screen("locations")
    object History : Screen("history")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    // Secondary Screens
    object AddItem : Screen("add_item")
    object EditItem : Screen("edit_item/{itemId}") {
        fun createRoute(itemId: String) = "edit_item/$itemId"
    }

    object AddLocation : Screen("add_location")
    object EditLocation : Screen("edit_location/{locationId}") {
        fun createRoute(locationId: String) = "edit_location/$locationId"
    }

    object ItemDetail : Screen("item_detail/{itemId}") {
        fun createRoute(itemId: String) = "item_detail/$itemId"
    }

    object LocationDetail : Screen("location_detail/{locationId}") {
        fun createRoute(locationId: String) = "location_detail/$locationId"
    }
}

// Bottom Navigation Items
enum class BottomNavItem(
    val screen: Screen,
    val title: String,
    val iconName: String
) {
    HOME(Screen.Home, "Home", "home"),
    ITEMS(Screen.Items, "Items", "items"),
    LOCATIONS(Screen.Locations, "Places", "locations"),
    HISTORY(Screen.History, "History", "history"),
    SETTINGS(Screen.Settings, "Settings", "settings")
}