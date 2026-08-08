package com.official.recipesnap

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import java.time.ZonedDateTime

class HealthConnectManager(private val context: Context) {
    
    val healthConnectClient: HealthConnectClient? by lazy { 
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getWritePermission(NutritionRecord::class)
    )

    suspend fun writeNutrition(recipe: RecipeOption) {
        val client = healthConnectClient ?: throw Exception("Health Connect is not available on this device.")
        val now = ZonedDateTime.now()
        val nutritionRecord = NutritionRecord(
            startTime = now.toInstant(),
            endTime = now.plusMinutes(1).toInstant(),
            startZoneOffset = now.offset,
            endZoneOffset = now.offset,
            energy = Energy.calories(recipe.calories),
            protein = Mass.grams(recipe.protein),
            totalCarbohydrate = Mass.grams(recipe.carbs),
            totalFat = Mass.grams(recipe.fat)
        )

        client.insertRecords(listOf(nutritionRecord))
    }
}
