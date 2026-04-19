package com.rajsv.exitsense.data.model


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ImportanceLevel(val label: String, val weight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4)
}

enum class DayOfWeek(val shortName: String, val fullName: String) {
    MONDAY("Mon", "Monday"),
    TUESDAY("Tue", "Tuesday"),
    WEDNESDAY("Wed", "Wednesday"),
    THURSDAY("Thu", "Thursday"),
    FRIDAY("Fri", "Friday"),
    SATURDAY("Sat", "Saturday"),
    SUNDAY("Sun", "Sunday")
}

enum class ReminderCondition(val label: String) {
    LEAVING_LOCATION("When leaving location"),
    TIME_BASED("At specific time"),
    LOW_BATTERY("When battery is low"),
    WEATHER_BASED("Based on weather"),
    ALWAYS("Always remind")
}

enum class ItemIcon(val label: String, val icon: ImageVector) {
    // Documents & Work
    ID_CARD("ID Card", Icons.Outlined.Badge),
    DOCUMENT("Document", Icons.Outlined.Description),
    FOLDER("Folder", Icons.Outlined.Folder),
    BOOK("Book", Icons.Outlined.MenuBook),
    NOTEBOOK("Notebook", Icons.Outlined.Book),

    // Electronics
    PHONE("Phone", Icons.Outlined.PhoneAndroid),
    LAPTOP("Laptop", Icons.Outlined.Laptop),
    CHARGER("Charger", Icons.Outlined.Cable),
    HEADPHONES("Headphones", Icons.Outlined.Headphones),
    WATCH("Watch", Icons.Outlined.Watch),
    CAMERA("Camera", Icons.Outlined.CameraAlt),

    // Personal Items
    WALLET("Wallet", Icons.Outlined.Wallet),
    KEYS("Keys", Icons.Outlined.Key),
    BAG("Bag", Icons.Outlined.Backpack),
    GLASSES("Glasses", Icons.Outlined.Visibility),
    UMBRELLA("Umbrella", Icons.Outlined.Umbrella),

    // Health & Fitness
    MEDICINE("Medicine", Icons.Outlined.Medication),
    WATER_BOTTLE("Water Bottle", Icons.Outlined.WaterDrop),
    GYM("Gym Gear", Icons.Outlined.FitnessCenter),

    // Food
    FOOD("Food", Icons.Outlined.Restaurant),
    LUNCH_BOX("Lunch Box", Icons.Outlined.LunchDining),
    COFFEE("Coffee", Icons.Outlined.Coffee),

    // Others
    GIFT("Gift", Icons.Outlined.CardGiftcard),
    SHOPPING("Shopping", Icons.Outlined.ShoppingBag),
    TICKET("Ticket", Icons.Outlined.ConfirmationNumber),
    TOOLS("Tools", Icons.Outlined.Build),
    OTHER("Other", Icons.Outlined.Category)
}

enum class LocationIcon(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    OFFICE("Office", Icons.Rounded.Business),
    SCHOOL("School", Icons.Rounded.School),
    COLLEGE("College", Icons.Rounded.AccountBalance),
    HOSTEL("Hostel", Icons.Rounded.Apartment),
    GYM("Gym", Icons.Rounded.FitnessCenter),
    LIBRARY("Library", Icons.Rounded.LocalLibrary),
    HOSPITAL("Hospital", Icons.Rounded.LocalHospital),
    RESTAURANT("Restaurant", Icons.Rounded.Restaurant),
    CAFE("Cafe", Icons.Rounded.Coffee),
    SHOP("Shop", Icons.Rounded.Store),
    FRIEND("Friend's Place", Icons.Rounded.People),
    PARK("Park", Icons.Rounded.Park),
    OTHER("Other", Icons.Rounded.Place)
}

enum class HistoryStatus(val label: String) {
    REMINDED("Reminded"),
    ACKNOWLEDGED("Acknowledged"),
    FORGOT("Forgot"),
    DISMISSED("Dismissed")
}