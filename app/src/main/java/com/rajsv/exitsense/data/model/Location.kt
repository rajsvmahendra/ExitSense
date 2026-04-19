package com.rajsv.exitsense.data.model

import java.util.UUID

data class Location(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: LocationIcon = LocationIcon.OTHER,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radiusInMeters: Int = 100,
    val isActive: Boolean = true,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Sample data for preview and testing
object SampleLocations {
    val hostel = Location(
        id = "loc_1",
        name = "Hostel",
        icon = LocationIcon.HOSTEL,
        address = "University Hostel, Block A",
        radiusInMeters = 50
    )

    val college = Location(
        id = "loc_2",
        name = "College",
        icon = LocationIcon.COLLEGE,
        address = "Main Campus Building",
        radiusInMeters = 150
    )

    val home = Location(
        id = "loc_3",
        name = "Home",
        icon = LocationIcon.HOME,
        address = "123 Main Street",
        radiusInMeters = 80
    )

    val gym = Location(
        id = "loc_4",
        name = "Gym",
        icon = LocationIcon.GYM,
        address = "Fitness Center",
        radiusInMeters = 60
    )

    val library = Location(
        id = "loc_5",
        name = "Library",
        icon = LocationIcon.LIBRARY,
        address = "Central Library",
        radiusInMeters = 100
    )

    val all = listOf(hostel, college, home, gym, library)
}