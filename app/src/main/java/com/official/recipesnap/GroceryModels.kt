package com.official.recipesnap

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class GroceryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String,
    val unit: String = "",
    val category: String = "Pantry",
    val isCompleted: Boolean = false,
    val sourceMealName: String? = null,
    val weekStartDate: String = "",
    val isSuggested: Boolean = false
)

object GroceryCategories {
    val PRODUCE = "Produce"
    val DAIRY_EGGS = "Dairy & Eggs"
    val PANTRY = "Pantry"
    val BAKERY = "Bakery"
    val PROTEIN = "Protein"
    val FROZEN = "Frozen"
    val BEVERAGES = "Beverages"
    val OTHER = "Other"
    
    val ALL = listOf(PRODUCE, DAIRY_EGGS, PANTRY, BAKERY, PROTEIN, FROZEN, BEVERAGES, OTHER)
}
