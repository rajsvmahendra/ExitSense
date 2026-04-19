package com.rajsv.exitsense.data.model

import java.util.UUID

data class ReminderItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: ItemIcon = ItemIcon.OTHER,
    val locationId: String? = null,
    val locationName: String? = null,
    val daysNeeded: List<DayOfWeek> = emptyList(),
    val reminderTime: String? = null, // Format: "HH:mm"
    val condition: ReminderCondition = ReminderCondition.LEAVING_LOCATION,
    val importance: ImportanceLevel = ImportanceLevel.MEDIUM,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastRemindedAt: Long? = null,
    val timesReminded: Int = 0,
    val timesForgotten: Int = 0,
    val isPinned: Boolean = false
)

// Sample data for preview and testing
object SampleItems {
    val idCard = ReminderItem(
        id = "item_1",
        name = "ID Card",
        icon = ItemIcon.ID_CARD,
        locationId = "loc_1",
        locationName = "Hostel",
        daysNeeded = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        ),
        importance = ImportanceLevel.CRITICAL,
        condition = ReminderCondition.LEAVING_LOCATION
    )

    val labFile = ReminderItem(
        id = "item_2",
        name = "Lab File",
        icon = ItemIcon.FOLDER,
        locationId = "loc_1",
        locationName = "Hostel",
        daysNeeded = listOf(DayOfWeek.THURSDAY),
        reminderTime = "08:30",
        importance = ImportanceLevel.HIGH,
        condition = ReminderCondition.TIME_BASED
    )

    val charger = ReminderItem(
        id = "item_3",
        name = "Phone Charger",
        icon = ItemIcon.CHARGER,
        locationId = "loc_2",
        locationName = "College",
        importance = ImportanceLevel.MEDIUM,
        condition = ReminderCondition.LOW_BATTERY
    )

    val laptop = ReminderItem(
        id = "item_4",
        name = "Laptop",
        icon = ItemIcon.LAPTOP,
        locationId = "loc_1",
        locationName = "Hostel",
        daysNeeded = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.FRIDAY
        ),
        importance = ImportanceLevel.HIGH,
        condition = ReminderCondition.LEAVING_LOCATION
    )

    val umbrella = ReminderItem(
        id = "item_5",
        name = "Umbrella",
        icon = ItemIcon.UMBRELLA,
        importance = ImportanceLevel.LOW,
        condition = ReminderCondition.WEATHER_BASED
    )

    val gymBag = ReminderItem(
        id = "item_6",
        name = "Gym Bag",
        icon = ItemIcon.GYM,
        locationId = "loc_3",
        locationName = "Home",
        daysNeeded = listOf(
            DayOfWeek.TUESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.SATURDAY
        ),
        importance = ImportanceLevel.MEDIUM,
        condition = ReminderCondition.LEAVING_LOCATION
    )

    val wallet = ReminderItem(
        id = "item_7",
        name = "Wallet",
        icon = ItemIcon.WALLET,
        importance = ImportanceLevel.CRITICAL,
        condition = ReminderCondition.ALWAYS
    )

    val all = listOf(idCard, labFile, charger, laptop, umbrella, gymBag, wallet)

    val todayEssentials = listOf(idCard, laptop, wallet)
}