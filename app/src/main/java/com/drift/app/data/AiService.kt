package com.drift.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the Drift backend for AI features.
 * Falls back to simple local logic when backend is unreachable.
 */
class AiService(var baseUrl: String = DEFAULT_BASE_URL) {

    companion object {
        // TODO: Update to your deployed backend URL
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"

        @Volatile
        private var INSTANCE: AiService? = null

        fun getInstance(): AiService {
            return INSTANCE ?: synchronized(this) {
                AiService().also { INSTANCE = it }
            }
        }
    }

    /**
     * Send raw brain dump text to backend, get structured items back.
     */
    suspend fun parseDump(text: String): List<ParsedItem> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("text", text) }
            val response = post("$baseUrl/parse-dump", body)
            val items = response.getJSONArray("items")

            (0 until items.length()).map { i ->
                val obj = items.getJSONObject(i)
                ParsedItem(
                    text = obj.getString("text"),
                    category = obj.getString("category")
                )
            }
        } catch (e: Exception) {
            fallbackParse(text)
        }
    }

    /**
     * Get daily focus recommendation from backend.
     */
    suspend fun getDailyFocus(
        items: List<DriftItem>,
        goals: List<DriftItem>
    ): String = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("items", JSONArray().apply {
                    items.forEach { item ->
                        put(JSONObject().apply {
                            put("text", item.text)
                            put("created_at", item.createdAt)
                            put("last_touched_at", item.lastTouchedAt)
                        })
                    }
                })
                put("goals", JSONArray().apply {
                    goals.forEach { goal ->
                        put(JSONObject().apply {
                            put("text", goal.text)
                            put("horizon", goal.goalHorizon ?: "week")
                        })
                    }
                })
            }
            val response = post("$baseUrl/daily-focus", body)
            response.getString("focus")
        } catch (e: Exception) {
            fallbackFocus(items, goals)
        }
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 10_000
        conn.readTimeout = 30_000
        conn.doOutput = true
        conn.outputStream.write(body.toString().toByteArray())

        val responseText = conn.inputStream.bufferedReader().readText()
        return JSONObject(responseText)
    }

    // --- Fallbacks (when backend is down) ---

    internal fun fallbackParse(text: String): List<ParsedItem> {
        return text.replace("\n", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.length > 2 }
            .map { ParsedItem(text = it, category = "task") }
    }

    internal fun fallbackFocus(items: List<DriftItem>, goals: List<DriftItem>): String {
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

data class ParsedItem(
    val text: String,
    val category: String
)
