package com.official.recipesnap

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MealPlanRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("meal_plan_prefs", Context.MODE_PRIVATE)
    private val MY_MEALS_KEY = "my_meals"
    private val PLANNED_MEALS_KEY = "planned_meals"
    private val SETUP_COMPLETED_KEY = "setup_completed"
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true 
    }

    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean(SETUP_COMPLETED_KEY, false)
    }

    fun setSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean(SETUP_COMPLETED_KEY, completed).apply()
    }

    // --- My Meals ---
    
    fun getMyMeals(): List<MyMeal> {
        val string = prefs.getString(MY_MEALS_KEY, null)
        return if (string != null) {
            try {
                json.decodeFromString<List<MyMeal>>(string)
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    fun saveMyMeal(meal: MyMeal) {
        val meals = getMyMeals().toMutableList()
        meals.add(meal)
        prefs.edit().putString(MY_MEALS_KEY, json.encodeToString(meals)).apply()
    }

    fun saveMyMeals(newMeals: List<MyMeal>) {
        val meals = getMyMeals().toMutableList()
        meals.addAll(newMeals)
        prefs.edit().putString(MY_MEALS_KEY, json.encodeToString(meals)).apply()
    }

    fun deleteMyMeal(mealId: String) {
        val meals = getMyMeals().filter { it.id != mealId }
        prefs.edit().putString(MY_MEALS_KEY, json.encodeToString(meals)).apply()
    }
    
    fun updateMyMeal(updatedMeal: MyMeal) {
        val meals = getMyMeals().toMutableList()
        val index = meals.indexOfFirst { it.id == updatedMeal.id }
        if (index != -1) {
            meals[index] = updatedMeal
            prefs.edit().putString(MY_MEALS_KEY, json.encodeToString(meals)).apply()
        }
    }

    // --- Planned Meals ---

    fun getPlannedMeals(): List<PlannedMeal> {
        val string = prefs.getString(PLANNED_MEALS_KEY, null)
        return if (string != null) {
            try {
                json.decodeFromString<List<PlannedMeal>>(string)
            } catch (e: Exception) {
                emptyList()
            }
        } else emptyList()
    }

    fun savePlannedMeal(plannedMeal: PlannedMeal) {
        val plans = getPlannedMeals().toMutableList()
        plans.add(plannedMeal)
        prefs.edit().putString(PLANNED_MEALS_KEY, json.encodeToString(plans)).apply()
    }

    fun deletePlannedMeal(plannedMealId: String) {
        val plans = getPlannedMeals().filter { it.id != plannedMealId }
        prefs.edit().putString(PLANNED_MEALS_KEY, json.encodeToString(plans)).apply()
    }
}
