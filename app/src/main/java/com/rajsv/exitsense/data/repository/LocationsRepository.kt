package com.rajsv.exitsense.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rajsv.exitsense.data.model.Location
import com.rajsv.exitsense.data.model.LocationIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.locationsDataStore: DataStore<Preferences> by preferencesDataStore(name = "locations_data")

object LocationsRepository {

    private val LOCATIONS_KEY = stringPreferencesKey("saved_locations")

    fun getLocations(context: Context): Flow<List<Location>> {
        return context.locationsDataStore.data.map { preferences ->
            val jsonString = preferences[LOCATIONS_KEY] ?: "[]"
            parseLocationsFromJson(jsonString)
        }
    }

    suspend fun addLocation(context: Context, location: Location) {
        context.locationsDataStore.edit { preferences ->
            val currentJson = preferences[LOCATIONS_KEY] ?: "[]"
            val locations = parseLocationsFromJson(currentJson).toMutableList()
            locations.add(location)
            preferences[LOCATIONS_KEY] = locationsToJson(locations)
        }
    }

    suspend fun updateLocation(context: Context, location: Location) {
        context.locationsDataStore.edit { preferences ->
            val currentJson = preferences[LOCATIONS_KEY] ?: "[]"
            val locations = parseLocationsFromJson(currentJson).toMutableList()
            val index = locations.indexOfFirst { it.id == location.id }
            if (index != -1) {
                locations[index] = location
            }
            preferences[LOCATIONS_KEY] = locationsToJson(locations)
        }
    }

    suspend fun deleteLocation(context: Context, locationId: String) {
        context.locationsDataStore.edit { preferences ->
            val currentJson = preferences[LOCATIONS_KEY] ?: "[]"
            val locations = parseLocationsFromJson(currentJson).toMutableList()
            locations.removeAll { it.id == locationId }
            preferences[LOCATIONS_KEY] = locationsToJson(locations)
        }
    }

    suspend fun clearAllLocations(context: Context) {
        context.locationsDataStore.edit { preferences ->
            preferences[LOCATIONS_KEY] = "[]"
        }
    }

    private fun parseLocationsFromJson(jsonString: String): List<Location> {
        val locations = mutableListOf<Location>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                locations.add(
                    Location(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", ""),
                        icon = try { LocationIcon.valueOf(obj.optString("icon", "OTHER")) } catch (e: Exception) { LocationIcon.OTHER },
                        address = obj.optString("address", ""),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        radiusInMeters = obj.optInt("radiusInMeters", 100),
                        isActive = obj.optBoolean("isActive", true),
                        isPinned = obj.optBoolean("isPinned", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locations
    }

    private fun locationsToJson(locations: List<Location>): String {
        val jsonArray = JSONArray()
        locations.forEach { location ->
            val obj = JSONObject().apply {
                put("id", location.id)
                put("name", location.name)
                put("icon", location.icon.name)
                put("address", location.address)
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("radiusInMeters", location.radiusInMeters)
                put("isActive", location.isActive)
                put("isPinned", location.isPinned)
                put("createdAt", location.createdAt)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}