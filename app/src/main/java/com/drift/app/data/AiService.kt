package com.drift.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class ParsedItem(val text: String, val category: String, val goalHorizon: String? = null, val parentGoal: String? = null)

data class DumpUpdate(val id: Long, val action: String)

data class DumpResult(
    val newItems: List<ParsedItem>,
    val updates: List<DumpUpdate>
)

data class FocusAction(val text: String, val why: String, val goal: String?)

data class DailyFocus(
    val greeting: String,
    val actions: List<FocusAction>,
    val parked: List<String>,
    val isFallback: Boolean = false
)

class AiService(var baseUrl: String = DEFAULT_BASE_URL) {

    companion object {
        const val DEFAULT_BASE_URL = "https://drift-api-evce.onrender.com"
        private const val PREFS_NAME = "drift_prefs"
        private const val KEY_DEVICE_ID = "device_id"

        @Volatile
        private var INSTANCE: AiService? = null

        fun getInstance(): AiService {
            return INSTANCE ?: synchronized(this) {
                AiService().also { INSTANCE = it }
            }
        }
    }

    var deviceId: String = UUID.randomUUID().toString()

    fun initDeviceId(context: android.content.Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        deviceId = prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    suspend fun parseDump(text: String, existingItems: List<DriftItem> = emptyList()): DumpResult = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("text", text)
                put("existing_items", JSONArray().apply {
                    existingItems.forEach { item ->
                        put(JSONObject().apply {
                            put("id", item.id)
                            put("text", item.text)
                            put("category", item.category ?: "task")
                            put("is_goal", item.isGoal)
                        })
                    }
                })
            }
            val response = post("$baseUrl/parse-dump", body)

            val newItems = response.optJSONArray("new_items") ?: JSONArray()
            val updates = response.optJSONArray("updates") ?: JSONArray()

            DumpResult(
                newItems = (0 until newItems.length()).map { i ->
                    val obj = newItems.getJSONObject(i)
                    ParsedItem(
                        text = obj.getString("text"),
                        category = obj.getString("category"),
                        goalHorizon = if (obj.isNull("goal_horizon")) null else obj.optString("goal_horizon"),
                        parentGoal = if (obj.isNull("parent_goal")) null else obj.optString("parent_goal")
                    )
                },
                updates = (0 until updates.length()).map { i ->
                    val obj = updates.getJSONObject(i)
                    DumpUpdate(id = obj.getLong("id"), action = obj.getString("action"))
                }
            )
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
            fallbackFocus(items, goals).copy(isFallback = true)
        }
    }

    private fun parseFocusResponse(json: JSONObject): DailyFocus {
        val actions = json.getJSONArray("actions")
        val parked = json.optJSONArray("parked") ?: JSONArray()
        val source = json.optString("source", "ai")

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
            parked = (0 until parked.length()).map { parked.getString(it) },
            isFallback = source != "ai"
        )
    }

    private fun post(url: String, body: JSONObject): JSONObject {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("X-Device-Id", deviceId)
        conn.connectTimeout = 10_000
        conn.readTimeout = 30_000
        conn.doOutput = true
        conn.outputStream.write(body.toString().toByteArray())
        val responseText = conn.inputStream.bufferedReader().readText()
        return JSONObject(responseText)
    }

    // --- Fallbacks ---

    internal fun fallbackParse(text: String): DumpResult {
        val entries = text.replace("\n", ",")
            .split(",")
            .map { it.trim() }
            .filter { it.length > 2 }
        return DumpResult(
            newItems = entries.map { ParsedItem(text = it, category = "task") },
            updates = emptyList()
        )
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
