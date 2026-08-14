package com.official.recipesnap

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GroceryRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("grocery_prefs", Context.MODE_PRIVATE)
    private val GROCERY_ITEMS_KEY = "grocery_items"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true 
    }

    fun getGroceryItems(): List<GroceryItem> {
        val string = prefs.getString(GROCERY_ITEMS_KEY, null)
        return if (string != null) {
            try {
                json.decodeFromString<List<GroceryItem>>(string)
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    fun saveGroceryItem(item: GroceryItem) {
        val items = getGroceryItems().toMutableList()
        items.add(item)
        prefs.edit().putString(GROCERY_ITEMS_KEY, json.encodeToString(items)).apply()
    }

    fun saveGroceryItems(newItems: List<GroceryItem>) {
        val items = getGroceryItems().toMutableList()
        items.addAll(newItems)
        prefs.edit().putString(GROCERY_ITEMS_KEY, json.encodeToString(items)).apply()
    }

    fun updateGroceryItem(updatedItem: GroceryItem) {
        val items = getGroceryItems().toMutableList()
        val index = items.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            items[index] = updatedItem
            prefs.edit().putString(GROCERY_ITEMS_KEY, json.encodeToString(items)).apply()
        }
    }
    
    fun deleteGroceryItem(itemId: String) {
        val items = getGroceryItems().filter { it.id != itemId }
        prefs.edit().putString(GROCERY_ITEMS_KEY, json.encodeToString(items)).apply()
    }

    fun uncheckAllForWeek(weekStartDate: String) {
        val items = getGroceryItems().map { 
            if (it.weekStartDate == weekStartDate && !it.isSuggested) {
                it.copy(isCompleted = false)
            } else {
                it
            }
        }
        prefs.edit().putString(GROCERY_ITEMS_KEY, json.encodeToString(items)).apply()
    }
}
