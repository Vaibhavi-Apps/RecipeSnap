package com.official.recipesnap

import kotlinx.serialization.Serializable

@Serializable
data class RecipeWrapper(val recipe: String)

@Serializable
data class GeminiResponse(val candidates: List<Candidate>)

@Serializable
data class Candidate(val content: Content)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)