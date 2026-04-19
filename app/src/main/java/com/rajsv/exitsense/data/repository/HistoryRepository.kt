package com.rajsv.exitsense.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rajsv.exitsense.data.model.HistoryEntry
import com.rajsv.exitsense.data.model.HistoryStatus
import com.rajsv.exitsense.data.model.ItemIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "history_data")

// Serializable version of HistoryEntry for DataStore
data class SerializableHistoryEntry(
    val id: String,
    val itemId: String,
    val itemName: String,
    val itemIconName: String,  // Store icon name as String
    val locationName: String?,
    val status: String,  // Store status as String
    val timestamp: Long,
    val date: String,
    val time: String
)

object HistoryRepository {

    private val HISTORY_KEY = stringPreferencesKey("history_entries")
    private val gson = Gson()

    // Get all history entries
    fun getHistoryEntries(context: Context): Flow<List<HistoryEntry>> {
        return context.historyDataStore.data.map { preferences ->
            val json = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<List<SerializableHistoryEntry>>() {}.type
            val serializableEntries: List<SerializableHistoryEntry> = gson.fromJson(json, type)

            serializableEntries.map { it.toHistoryEntry() }
                .sortedByDescending { it.timestamp }
        }
    }

    // Add new history entry
    suspend fun addHistoryEntry(
        context: Context,
        itemId: String,
        itemName: String,
        itemIcon: ItemIcon,
        locationName: String?,
        status: HistoryStatus
    ) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<SerializableHistoryEntry>>() {}.type
            val entries: MutableList<SerializableHistoryEntry> = gson.fromJson(json, type)

            val newEntry = SerializableHistoryEntry(
                id = UUID.randomUUID().toString(),
                itemId = itemId,
                itemName = itemName,
                itemIconName = itemIcon.name,  // Store enum name
                locationName = locationName,
                status = status.name,  // Store enum name
                timestamp = System.currentTimeMillis(),
                date = getCurrentDate(),
                time = getCurrentTime()
            )

            entries.add(0, newEntry)

            // Keep only last 100 entries
            if (entries.size > 100) {
                entries.removeAt(entries.size - 1)
            }

            preferences[HISTORY_KEY] = gson.toJson(entries)
        }
    }

    // Clear all history
    suspend fun clearHistory(context: Context) {
        context.historyDataStore.edit { preferences ->
            preferences[HISTORY_KEY] = "[]"
        }
    }

    // Delete specific entry
    suspend fun deleteHistoryEntry(context: Context, entryId: String) {
        context.historyDataStore.edit { preferences ->
            val json = preferences[HISTORY_KEY] ?: "[]"
            val type = object : TypeToken<MutableList<SerializableHistoryEntry>>() {}.type
            val entries: MutableList<SerializableHistoryEntry> = gson.fromJson(json, type)

            entries.removeAll { it.id == entryId }

            preferences[HISTORY_KEY] = gson.toJson(entries)
        }
    }

    // Get history for specific item
    fun getHistoryForItem(context: Context, itemId: String): Flow<List<HistoryEntry>> {
        return getHistoryEntries(context).map { entries ->
            entries.filter { it.itemId == itemId }
        }
    }

    // Get history statistics
    fun getHistoryStats(context: Context): Flow<HistoryStats> {
        return getHistoryEntries(context).map { entries ->
            HistoryStats(
                totalReminders = entries.size,
                acknowledged = entries.count { it.status == HistoryStatus.ACKNOWLEDGED },
                forgotten = entries.count { it.status == HistoryStatus.FORGOT },
                dismissed = entries.count { it.status == HistoryStatus.DISMISSED }
            )
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}

data class HistoryStats(
    val totalReminders: Int,
    val acknowledged: Int,
    val forgotten: Int,
    val dismissed: Int
) {
    val successRate: Int
        get() = if (totalReminders > 0) {
            ((acknowledged.toFloat() / totalReminders) * 100).toInt()
        } else 0
}

// Extension function to convert SerializableHistoryEntry to HistoryEntry
private fun SerializableHistoryEntry.toHistoryEntry(): HistoryEntry {
    val itemIcon = try {
        ItemIcon.valueOf(this.itemIconName)
    } catch (e: Exception) {
        ItemIcon.OTHER  // Fallback icon
    }

    val historyStatus = try {
        HistoryStatus.valueOf(this.status)
    } catch (e: Exception) {
        HistoryStatus.REMINDED  // Fallback status
    }

    return HistoryEntry(
        id = this.id,
        itemId = this.itemId,
        itemName = this.itemName,
        itemIcon = itemIcon,
        locationName = this.locationName,
        status = historyStatus,
        timestamp = this.timestamp,
        date = this.date,
        time = this.time
    )
}