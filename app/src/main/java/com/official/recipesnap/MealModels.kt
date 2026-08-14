package com.official.recipesnap

import kotlinx.serialization.Serializable
import java.util.UUID

enum class MealCategory {
    BREAKFAST, LUNCH, DINNER, SNACK
}

@Serializable
data class MyMeal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: MealCategory,
    val imageUri: String? = null,
    val cookingTime: String? = null,
    val calories: Int? = null,
    val isCustom: Boolean = false,
    val defaultIngredients: List<GroceryItem> = emptyList()
)

@Serializable
data class PlannedMeal(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // format "yyyy-MM-dd"
    val mealCategory: MealCategory,
    val myMealId: String
)
