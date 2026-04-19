package com.rajsv.exitsense.data.model

import java.util.UUID

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val itemName: String,
    val itemIcon: ItemIcon,
    val locationName: String?,
    val status: HistoryStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // Format: "dd MMM yyyy"
    val time: String  // Format: "hh:mm a"
)

// Sample history data for preview
object SampleHistory {
    val entries = listOf(
        HistoryEntry(
            id = "hist_1",
            itemId = "item_1",
            itemName = "ID Card",
            itemIcon = ItemIcon.ID_CARD,
            locationName = "Hostel",
            status = HistoryStatus.ACKNOWLEDGED,
            date = "15 Jan 2025",
            time = "08:30 AM"
        ),
        HistoryEntry(
            id = "hist_2",
            itemId = "item_2",
            itemName = "Lab File",
            itemIcon = ItemIcon.FOLDER,
            locationName = "Hostel",
            status = HistoryStatus.FORGOT,
            date = "14 Jan 2025",
            time = "09:15 AM"
        ),
        HistoryEntry(
            id = "hist_3",
            itemId = "item_4",
            itemName = "Laptop",
            itemIcon = ItemIcon.LAPTOP,
            locationName = "Hostel",
            status = HistoryStatus.ACKNOWLEDGED,
            date = "14 Jan 2025",
            time = "08:45 AM"
        ),
        HistoryEntry(
            id = "hist_4",
            itemId = "item_7",
            itemName = "Wallet",
            itemIcon = ItemIcon.WALLET,
            locationName = "Home",
            status = HistoryStatus.REMINDED,
            date = "13 Jan 2025",
            time = "07:30 AM"
        ),
        HistoryEntry(
            id = "hist_5",
            itemId = "item_3",
            itemName = "Phone Charger",
            itemIcon = ItemIcon.CHARGER,
            locationName = "College",
            status = HistoryStatus.DISMISSED,
            date = "13 Jan 2025",
            time = "05:00 PM"
        ),
        HistoryEntry(
            id = "hist_6",
            itemId = "item_6",
            itemName = "Gym Bag",
            itemIcon = ItemIcon.GYM,
            locationName = "Home",
            status = HistoryStatus.ACKNOWLEDGED,
            date = "12 Jan 2025",
            time = "06:00 PM"
        )
    )
}