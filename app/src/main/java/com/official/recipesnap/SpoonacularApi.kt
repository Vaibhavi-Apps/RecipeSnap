package com.official.recipesnap

import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

@Serializable
data class SpoonacularResponse(
    val results: List<RecipeDto>
)

@Serializable
data class RecipeDto(
    val id: Int,
    val title: String,
    val image: String? = null,
    val readyInMinutes: Int? = null,
    val healthScore: Double? = null,
    val vegetarian: Boolean = false,
    val vegan: Boolean = false,
    val glutenFree: Boolean = false,
    val nutrition: NutritionDto? = null
)

@Serializable
data class NutritionDto(
    val nutrients: List<NutrientDto>
)

@Serializable
data class NutrientDto(
    val name: String,
    val amount: Double,
    val unit: String
)

interface SpoonacularApi {
    @GET("recipes/complexSearch")
    suspend fun searchRecipes(
        @Query("apiKey") apiKey: String = BuildConfig.SPOONACULAR_API_KEY,
        @Query("query") query: String? = null,
        @Query("type") type: String? = null,
        @Query("addRecipeNutrition") addRecipeNutrition: Boolean = true,
        @Query("addRecipeInformation") addRecipeInformation: Boolean = true,
        @Query("number") number: Int = 15
    ): SpoonacularResponse
    
    companion object {
        private const val BASE_URL = "https://api.spoonacular.com/"

        fun create(): SpoonacularApi {
            val json = Json { ignoreUnknownKeys = true }
            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(SpoonacularApi::class.java)
        }
    }
}
