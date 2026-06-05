package com.example.api

import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Self-contained JSON builder and parser to avoid any kotlinx-serialization or Moshi mismatch
    fun askGeminiDirect(prompt: String, apiKey: String, systemPrompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        
        // Escape quotes to construct a safe raw JSON string payload
        val escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        val escapedSystemPrompt = systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")

        val jsonRequest = """
            {
               "contents": [
                  {
                     "parts": [
                        {"text": "$escapedPrompt"}
                     ]
                  }
               ],
               "systemInstruction": {
                  "parts": [
                     {"text": "$escapedSystemPrompt"}
                  ]
               }
            }
        """.trimIndent()

        val mediaType = "application/json".toMediaType()
        val body = jsonRequest.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val responseBodyStr = response.body?.string() ?: return null
                
                // Extremely robust substring extraction to find "text": "..." content 
                val textMarker = "\"text\":"
                val markerIndex = responseBodyStr.indexOf(textMarker)
                if (markerIndex == -1) return null
                
                val startIndex = responseBodyStr.indexOf("\"", markerIndex + textMarker.length)
                if (startIndex == -1) return null
                
                val endIndex = responseBodyStr.indexOf("\"", startIndex + 1)
                if (endIndex == -1) return null
                
                val extractedText = responseBodyStr.substring(startIndex + 1, endIndex)
                // Unescape typical JS/JSON escapes
                extractedText.replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            }
        } catch (e: Exception) {
            null
        }
    }
}

suspend fun askGemini(prompt: String, fallbackText: String, systemPrompt: String = "أنت المساعد الذكي لتطبيق 'كل الخدمات بين يديك'. تجيب عن أسئلة المستخدمين بلطف وسرعة وبلغة عربية مفهومة."): String {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
        return fallbackText
    }

    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val result = GeminiApiClient.askGeminiDirect(prompt, apiKey, systemPrompt)
        result ?: fallbackText
    }
}
