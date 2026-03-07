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
        service = AiService(baseUrl = "http://localhost:0")
    }

    // --- Fallback Parse ---

    @Test
    fun `fallback parse splits comma-separated items`() {
        val result = service.fallbackParse("file taxes, finish book, call mom")
        assertEquals(3, result.newItems.size)
        assertEquals("file taxes", result.newItems[0].text)
    }

    @Test
    fun `fallback parse filters short fragments`() {
        val result = service.fallbackParse("file taxes, ok, , finish book")
        assertEquals(2, result.newItems.size)
    }

    @Test
    fun `fallback parse assigns task category`() {
        val result = service.fallbackParse("file taxes, start running")
        assertTrue(result.newItems.all { it.category == "task" })
        assertTrue(result.updates.isEmpty())
    }

    // --- Fallback Focus ---

    @Test
    fun `fallback focus returns structured data with stale item`() {
        val items = listOf(
            DriftItem(id = 1, text = "File taxes",
                lastTouchedAt = LocalDateTime.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            DriftItem(id = 2, text = "Go running",
                lastTouchedAt = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        )
        val result = service.fallbackFocus(items, emptyList())

        assertNotNull(result.greeting)
        assertEquals(1, result.actions.size)
        assertEquals("File taxes", result.actions[0].text)
        assertTrue(result.parked.contains("Go running"))
    }

    @Test
    fun `fallback focus prompts when only goals exist`() {
        val goals = listOf(DriftItem(id = 1, text = "Ship MVP", isGoal = true, goalHorizon = "month"))
        val result = service.fallbackFocus(emptyList(), goals)

        assertEquals(1, result.actions.size)
        assertEquals("Ship MVP", result.actions[0].goal)
    }

    @Test
    fun `fallback focus prompts to dump when empty`() {
        val result = service.fallbackFocus(emptyList(), emptyList())
        assertTrue(result.actions[0].text.contains("Dump", ignoreCase = true))
    }

    // --- Network fallback ---

    @Test
    fun `getDailyFocus falls back when backend unreachable`() = runBlocking {
        val items = listOf(
            DriftItem(id = 1, text = "Stale task",
                lastTouchedAt = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        )
        val result = service.getDailyFocus(items, emptyList())
        assertEquals("Stale task", result.actions[0].text)
    }

    @Test
    fun `parseDump falls back when backend unreachable`() = runBlocking {
        val result = service.parseDump("file taxes, read a book")
        assertEquals(2, result.newItems.size)
        assertTrue(result.updates.isEmpty())
    }
}
