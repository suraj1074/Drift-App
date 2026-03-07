package com.drift.app.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AiServiceTest {

    private lateinit var service: AiService

    @Before
    fun setup() {
        service = AiService(apiKey = "")
    }

    @Test
    fun `fallback suggests stale item when tasks exist`() = runBlocking {
        val items = listOf(
            DriftItem(
                id = 1,
                text = "File taxes",
                lastTouchedAt = LocalDateTime.now().minusDays(10)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ),
            DriftItem(
                id = 2,
                text = "Go running",
                lastTouchedAt = LocalDateTime.now().minusDays(2)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )
        val goals = listOf(
            DriftItem(id = 3, text = "Get fit", isGoal = true, goalHorizon = "month")
        )

        val result = service.getDailyFocus(items, goals)

        assertTrue("Should mention the stale item", result.contains("File taxes"))
    }

    @Test
    fun `fallback prompts to add tasks when only goals exist`() = runBlocking {
        val items = emptyList<DriftItem>()
        val goals = listOf(
            DriftItem(id = 1, text = "Ship MVP", isGoal = true, goalHorizon = "month")
        )

        val result = service.getDailyFocus(items, goals)

        assertTrue("Should prompt about goals without tasks", result.contains("goals"))
    }

    @Test
    fun `fallback prompts to dump when nothing exists`() = runBlocking {
        val result = service.getDailyFocus(emptyList(), emptyList())

        assertTrue("Should prompt to add items", result.contains("dump") || result.contains("plate"))
    }

    @Test
    fun `fallback picks oldest item as most stale`() = runBlocking {
        val items = listOf(
            DriftItem(
                id = 1,
                text = "Recent task",
                lastTouchedAt = LocalDateTime.now().minusDays(1)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ),
            DriftItem(
                id = 2,
                text = "Old forgotten task",
                lastTouchedAt = LocalDateTime.now().minusDays(30)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )

        val result = service.getDailyFocus(items, emptyList())

        assertTrue("Should pick the oldest item", result.contains("Old forgotten task"))
    }
}
