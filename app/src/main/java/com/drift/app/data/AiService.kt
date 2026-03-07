package com.drift.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Calls OpenAI API to get daily focus recommendations.
 * For prototype — replace API key with your own.
 */
class AiService(var apiKey: String = "") {

    companion object {
        @Volatile
        private var INSTANCE: AiService? = null

        fun getInstance(): AiService {
            return INSTANCE ?: synchronized(this) {
                AiService().also { INSTANCE = it }
            }
        }
    }

    suspend fun getDailyFocus(
        items: List<DriftItem>,
        goals: List<DriftItem>
    ): String = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            return@withContext fallbackFocus(items, goals)
        }

        val prompt = buildPrompt(items, goals)

        try {
            val body = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", """You are Drift, a calm and thoughtful AI companion. 
                            |Your job is to look at everything on someone's plate and their goals, 
                            |then pick the 1-2 things they should focus on TODAY. 
                            |Be specific, warm, and brief. No bullet lists — write like a friend texting. 
                            |If something has been idle for a while, mention it gently.""".trimMargin())
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
                put("max_tokens", 300)
                put("temperature", 0.7)
            }

            val conn = URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray())

            val response = conn.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } catch (e: Exception) {
            fallbackFocus(items, goals)
        }
    }

    private fun buildPrompt(items: List<DriftItem>, goals: List<DriftItem>): String {
        val goalsText = if (goals.isEmpty()) "No goals set yet."
        else goals.joinToString("\n") { "- ${it.text} (${it.goalHorizon ?: "general"})" }

        val itemsText = if (items.isEmpty()) "Nothing on their plate yet."
        else items.joinToString("\n") { "- ${it.text} (added: ${it.createdAt.take(10)}, last touched: ${it.lastTouchedAt.take(10)})" }

        return """Here's what this person has on their plate:
            |
            |GOALS:
            |$goalsText
            |
            |TASKS & ITEMS:
            |$itemsText
            |
            |Based on their goals and what's been drifting, what should they focus on today? Pick 1-2 things max.""".trimMargin()
    }

    private fun fallbackFocus(items: List<DriftItem>, goals: List<DriftItem>): String {
        // Simple fallback when no API key: pick the most stale item
        val stale = items.sortedBy { it.lastTouchedAt }.firstOrNull()
        return if (stale != null) {
            "Hey — you haven't touched \"${stale.text}\" in a while. Maybe start there today?"
        } else if (goals.isNotEmpty()) {
            "You've set some goals but haven't added any tasks yet. What's one small step you could take today?"
        } else {
            "Nothing on your plate yet. Tap the + button and dump whatever's on your mind."
        }
    }
}
