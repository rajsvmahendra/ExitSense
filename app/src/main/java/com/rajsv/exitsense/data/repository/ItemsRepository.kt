package com.rajsv.exitsense.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rajsv.exitsense.data.model.DayOfWeek
import com.rajsv.exitsense.data.model.ImportanceLevel
import com.rajsv.exitsense.data.model.ItemIcon
import com.rajsv.exitsense.data.model.ReminderCondition
import com.rajsv.exitsense.data.model.ReminderItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.itemsDataStore: DataStore<Preferences> by preferencesDataStore(name = "items_data")

object ItemsRepository {

    private val ITEMS_KEY = stringPreferencesKey("saved_items")

    fun getItems(context: Context): Flow<List<ReminderItem>> {
        return context.itemsDataStore.data.map { preferences ->
            val jsonString = preferences[ITEMS_KEY] ?: "[]"
            parseItemsFromJson(jsonString)
        }
    }

    suspend fun addItem(context: Context, item: ReminderItem) {
        context.itemsDataStore.edit { preferences ->
            val currentJson = preferences[ITEMS_KEY] ?: "[]"
            val items = parseItemsFromJson(currentJson).toMutableList()
            items.add(item)
            preferences[ITEMS_KEY] = itemsToJson(items)
        }
    }

    suspend fun updateItem(context: Context, item: ReminderItem) {
        context.itemsDataStore.edit { preferences ->
            val currentJson = preferences[ITEMS_KEY] ?: "[]"
            val items = parseItemsFromJson(currentJson).toMutableList()
            val index = items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                items[index] = item
            }
            preferences[ITEMS_KEY] = itemsToJson(items)
        }
    }

    suspend fun deleteItem(context: Context, itemId: String) {
        context.itemsDataStore.edit { preferences ->
            val currentJson = preferences[ITEMS_KEY] ?: "[]"
            val items = parseItemsFromJson(currentJson).toMutableList()
            items.removeAll { it.id == itemId }
            preferences[ITEMS_KEY] = itemsToJson(items)
        }
    }

    suspend fun clearAllItems(context: Context) {
        context.itemsDataStore.edit { preferences ->
            preferences[ITEMS_KEY] = "[]"
        }
    }

    private fun parseItemsFromJson(jsonString: String): List<ReminderItem> {
        val items = mutableListOf<ReminderItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    ReminderItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        icon = try { ItemIcon.valueOf(obj.optString("icon", "OTHER")) } catch (e: Exception) { ItemIcon.OTHER },
                        locationId = obj.optString("locationId", null).takeIf { it != "null" && it.isNotEmpty() },
                        locationName = obj.optString("locationName", null).takeIf { it != "null" && it.isNotEmpty() },
                        daysNeeded = parseDaysFromJson(obj.optJSONArray("daysNeeded")),
                        reminderTime = obj.optString("reminderTime", null).takeIf { it != "null" && it.isNotEmpty() },
                        condition = try { ReminderCondition.valueOf(obj.optString("condition", "LEAVING_LOCATION")) } catch (e: Exception) { ReminderCondition.LEAVING_LOCATION },
                        importance = try { ImportanceLevel.valueOf(obj.optString("importance", "MEDIUM")) } catch (e: Exception) { ImportanceLevel.MEDIUM },
                        isActive = obj.optBoolean("isActive", true),
                        isPinned = obj.optBoolean("isPinned", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return items
    }

    private fun parseDaysFromJson(jsonArray: JSONArray?): List<DayOfWeek> {
        if (jsonArray == null) return emptyList()
        val days = mutableListOf<DayOfWeek>()
        for (i in 0 until jsonArray.length()) {
            try {
                days.add(DayOfWeek.valueOf(jsonArray.getString(i)))
            } catch (e: Exception) {
                // Skip invalid day
            }
        }
        return days
    }

    private fun itemsToJson(items: List<ReminderItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("icon", item.icon.name)
                put("locationId", item.locationId ?: "null")
                put("locationName", item.locationName ?: "null")
                put("daysNeeded", JSONArray(item.daysNeeded.map { it.name }))
                put("reminderTime", item.reminderTime ?: "null")
                put("condition", item.condition.name)
                put("importance", item.importance.name)
                put("isActive", item.isActive)
                put("isPinned", item.isPinned)
                put("createdAt", item.createdAt)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}