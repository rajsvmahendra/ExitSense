package com.rajsv.exitsense.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object ExitSenseShapes {

    // Small components - chips, tags, small buttons
    val small = RoundedCornerShape(8.dp)

    // Medium components - buttons, input fields
    val medium = RoundedCornerShape(12.dp)

    // Large components - cards, dialogs
    val large = RoundedCornerShape(16.dp)

    // Extra large - premium cards, bottom sheets
    val extraLarge = RoundedCornerShape(24.dp)

    // Hero cards - main feature cards
    val hero = RoundedCornerShape(28.dp)

    // Full rounded - pills, toggles
    val pill = RoundedCornerShape(50)

    // Bottom sheet shape
    val bottomSheet = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    // Top card shape
    val topCard = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
}