package com.drift.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ParsedItem(val text: String, val category: String)

data class FocusAction(val text: String, val why: String, val goal: String?)

data class DailyFocus(
    val greeting: String,
    val actions: List<FocusAction>,
    val parked: List<String>
)

class AiService(var baseUrl: String = DEFAULT_BASE_URL) {

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"

        @Volatile
        private var INSTANCE: AiService? = null

        fun getInstance(): AiService {
            return INSTANCE ?: synchronized(this) {
                AiService().also { INSTANCE = it }
            }
        }
    }

    suspend fun parseDump(text: String): List<ParsedItem> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply { put("text", text) }
            val response = post("$baseUrl/parse-dump", body)
            val items = response.getJSONArray("items")
            (0 until items.length()).map { i ->
                val obj = items.getJSONObject(i)
                ParsedItem(obj.getString("text"), obj.getString("category"))
            }
        } catch (e: Exception) {
            fallbackParse(text)
        }
    }

    suspend fun getDailyFocus(
        items: List<DriftItem>,
        goals: List<DriftItem>
    ): DailyFocus = withContext(Dispatchers.IO) {
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
            parseFocusResponse(response)
        } catch (e: Exception) {
            fallbackFocus(items, goals)
        }
    }

    private fun parseFocusResponse(json: JSONObject): DailyFocus {
        val actions = json.getJSONArray("actions")
        val parked = json.optJSONArray("parked") ?: JSONArray()

        return DailyFocus(
            greeting = json.getString("greeting"),
            actions = (0 until actions.length()).map { i ->
                val a = actions.getJSONObject(i)
                FocusAction(
                    text = a.getString("text"),
                    why = a.getString("why"),
                    goal = if (a.isNull("goal")) null else a.optString("goal")
                )
            },
            parked = (0 until parked.length()).map { parked.getString(it) }
        )
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

    // --- Fallbacks ---

    internal fun fallbackParse(text: String): List<ParsedItem> {
        return text.replace("\n", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.length > 2 }
            .map { ParsedItem(text = it, category = "task") }
    }

    internal fun fallbackFocus(items: List<DriftItem>, goals: List<DriftItem>): DailyFocus {
        val stale = items.sortedBy { it.lastTouchedAt }.firstOrNull()
        return if (stale != null) {
            DailyFocus(
                greeting = "Hey! Here's what I'd focus on today.",
                actions = listOf(FocusAction(stale.text, "This has been untouched the longest.", null)),
                parked = items.filter { it.id != stale.id }.take(3).map { it.text }
            )
        } else if (goals.isNotEmpty()) {
            DailyFocus(
                greeting = "You've set some goals — nice!",
                actions = listOf(FocusAction("Add a few tasks", "Goals need small steps.", goals[0].text)),
                parked = emptyList()
            )
        } else {
            DailyFocus(
                greeting = "Nothing on your plate yet.",
                actions = listOf(FocusAction("Dump whatever's on your mind", "That's how we start.", null)),
                parked = emptyList()
            )
        }
    }
}
