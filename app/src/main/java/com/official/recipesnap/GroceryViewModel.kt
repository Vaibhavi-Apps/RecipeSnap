package com.official.recipesnap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GroceryRepository(application)
    private val mealPlanViewModel = MealPlanViewModel(application) // For fetching planned meals

    private val _groceryItems = MutableStateFlow<List<GroceryItem>>(emptyList())
    val groceryItems: StateFlow<List<GroceryItem>> = _groceryItems.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(mealPlanViewModel.currentWeekStart.value)
    val currentWeekStart: StateFlow<Date> = _currentWeekStart.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val allItems = repository.getGroceryItems()
        val weekStr = mealPlanViewModel.formatDateForStorage(_currentWeekStart.value)
        val activeItems = allItems.filter { it.weekStartDate == weekStr && !it.isSuggested }
        
        // Generate suggestions from Meal Plan
        val weekDays = getWeekDays(_currentWeekStart.value)
        val allPlannedMeals = weekDays.flatMap { mealPlanViewModel.getPlannedMealsForDate(it) }
        
        val suggestedItems = mutableListOf<GroceryItem>()
        
        for (plan in allPlannedMeals) {
            val myMeal = mealPlanViewModel.getMyMealById(plan.myMealId)
            if (myMeal != null) {
                for (ingredient in myMeal.defaultIngredients) {
                    val normalizedName = normalizeIngredientName(ingredient.name)
                    // Check if already in active items
                    val existsInActive = activeItems.any { normalizeIngredientName(it.name) == normalizedName }
                    if (!existsInActive) {
                        // Check if already in suggested, if so, merge
                        val existingSuggested = suggestedItems.find { normalizeIngredientName(it.name) == normalizedName }
                        if (existingSuggested != null) {
                            val newQuantity = mergeQuantities(existingSuggested.quantity, ingredient.quantity)
                            val merged = existingSuggested.copy(
                                quantity = newQuantity,
                                sourceMealName = "${existingSuggested.sourceMealName} + ${myMeal.name}"
                            )
                            val index = suggestedItems.indexOf(existingSuggested)
                            suggestedItems[index] = merged
                        } else {
                            suggestedItems.add(
                                ingredient.copy(
                                    sourceMealName = myMeal.name,
                                    weekStartDate = weekStr,
                                    isSuggested = true
                                )
                            )
                        }
                    }
                }
            }
        }
        
        _groceryItems.value = activeItems + suggestedItems
    }

    fun toggleItemCompletion(item: GroceryItem) {
        if (item.isSuggested) return // Cannot toggle suggested directly, must add it first
        val updated = item.copy(isCompleted = !item.isCompleted)
        repository.updateGroceryItem(updated)
        loadData()
    }

    fun addManualItem(name: String, quantity: String, category: String) {
        val weekStr = mealPlanViewModel.formatDateForStorage(_currentWeekStart.value)
        val newItem = GroceryItem(
            name = name,
            quantity = quantity,
            category = category,
            weekStartDate = weekStr,
            isSuggested = false
        )
        repository.saveGroceryItem(newItem)
        loadData()
    }

    fun acceptSuggestion(item: GroceryItem) {
        val newItem = item.copy(isSuggested = false) // Add to active
        repository.saveGroceryItem(newItem)
        loadData()
    }

    fun uncheckAll() {
        val weekStr = mealPlanViewModel.formatDateForStorage(_currentWeekStart.value)
        repository.uncheckAllForWeek(weekStr)
        loadData()
    }

    fun nextWeek() {
        mealPlanViewModel.nextWeek()
        _currentWeekStart.value = mealPlanViewModel.currentWeekStart.value
        loadData()
    }

    fun previousWeek() {
        mealPlanViewModel.previousWeek()
        _currentWeekStart.value = mealPlanViewModel.currentWeekStart.value
        loadData()
    }
    
    fun formatDateRange(): String {
        return mealPlanViewModel.formatDateRange(_currentWeekStart.value)
    }

    private fun getWeekDays(startOfWeek: Date): List<Date> {
        val days = mutableListOf<Date>()
        val cal = java.util.Calendar.getInstance().apply { time = startOfWeek }
        for (i in 0 until 7) {
            days.add(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return days
    }

    private fun normalizeIngredientName(name: String): String {
        var lower = name.lowercase().trim()
        if (lower.endsWith("oes")) return lower.dropLast(2) // Tomatoes -> Tomato
        if (lower.endsWith("s")) return lower.dropLast(1) // Apples -> Apple
        return lower
    }

    private fun mergeQuantities(q1: String, q2: String): String {
        val n1 = q1.toIntOrNull()
        val n2 = q2.toIntOrNull()
        if (n1 != null && n2 != null) {
            return (n1 + n2).toString()
        }
        return "$q1 + $q2"
    }
    
    fun getFormattedClipboardText(): String {
        val activeItems = _groceryItems.value.filter { !it.isSuggested }
        val sb = StringBuilder()
        sb.appendLine("GROCERY LIST")
        sb.appendLine(formatDateRange())
        sb.appendLine()
        
        val grouped = activeItems.groupBy { it.category }
        for ((category, items) in grouped) {
            if (items.isNotEmpty()) {
                sb.appendLine(category)
                for (item in items) {
                    val status = if (item.isCompleted) "[x]" else "[ ]"
                    sb.appendLine("$status ${item.name} — ${item.quantity} ${item.unit}".trimEnd())
                }
                sb.appendLine()
            }
        }
        return sb.toString()
    }
}
