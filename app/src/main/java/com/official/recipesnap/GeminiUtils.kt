package com.official.recipesnap

import android.content.Context
import android.net.Uri/*
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.FilePart*/
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
/*

suspend fun generateRecipeFromImage(context: Context, imageUri: Uri): String {
   */
/* return withContext(Dispatchers.IO) {
        try {
            val apiKey = context.getString(R.string.gemini_api_key)
            val model = GenerativeModel(
                modelName = "gemini-pro-vision",
                apiKey = apiKey
            )

            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            requireNotNull(inputStream) { "Could not open image stream." }

            val imagePart = FilePart.from("image.jpg", inputStream)

            val prompt = content    {
                image(imagePart)
                text("Generate a detailed recipe based on the ingredients visible in this image.")
            }

            val response = model.generateContent(prompt)
            response.text ?: "❌ Gemini API did not return a recipe."
        } catch (e: Exception) {
            e.printStackTrace()
            "❌ Error generating recipe: ${e.localizedMessage}"
        }
    }*//*

}
*/
