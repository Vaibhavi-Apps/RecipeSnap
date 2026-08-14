package com.official.recipesnap

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SavedSnap(
    val id: String,
    val imageUri: String,
    val timestamp: Long,
    val recipes: List<RecipeOption>,
    val isFavorite: Boolean = false
)

class SnapRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recipe_snaps_prefs", Context.MODE_PRIVATE)
    private val SNAPS_KEY = "saved_snaps"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true 
    }

    fun getSnaps(): List<SavedSnap> {
        val snapsString = prefs.getString(SNAPS_KEY, null)
        return if (snapsString != null) {
            try {
                json.decodeFromString<List<SavedSnap>>(snapsString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveSnap(snap: SavedSnap) {
        val currentSnaps = getSnaps().toMutableList()
        currentSnaps.add(0, snap) // Add new snaps to the top of the list
        val newSnapsString = json.encodeToString(currentSnaps)
        prefs.edit().putString(SNAPS_KEY, newSnapsString).apply()
    }

    fun updateSnap(updatedSnap: SavedSnap) {
        val currentSnaps = getSnaps().toMutableList()
        val index = currentSnaps.indexOfFirst { it.id == updatedSnap.id }
        if (index != -1) {
            currentSnaps[index] = updatedSnap
            val newSnapsString = json.encodeToString(currentSnaps)
            prefs.edit().putString(SNAPS_KEY, newSnapsString).apply()
        }
    }
}
