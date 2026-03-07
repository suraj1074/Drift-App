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
        // Use a bad URL so it always falls back to local logic
        service = AiService(baseUrl = "http://localhost:0")
    }

    // --- Fallback Parse ---

    @Test
    fun `fallback parse splits comma-separated items`() {
        val result = service.fallbackParse("file taxes, finish book, call mom")
        assertEquals(3, result.size)
        assertEquals("file taxes", result[0].text)
        assertEquals("finish book", result[1].text)
        assertEquals("call mom", result[2].text)
    }

    @Test
    fun `fallback parse splits newline-separated items`() {
        val result = service.fallbackParse("file taxes\nfinish book\ncall mom")
        assertEquals(3, result.size)
    }

    @Test
    fun `fallback parse filters short fragments`() {
        val result = service.fallbackParse("file taxes, ok, , finish book")
        assertEquals(2, result.size)
    }

    @Test
    fun `fallback parse assigns task category to all items`() {
        val result = service.fallbackParse("file taxes, start running")
        assertTrue(result.all { it.category == "task" })
    }

    // --- Fallback Focus ---

    @Test
    fun `fallback focus suggests stale item when tasks exist`() {
        val items = listOf(
            DriftItem(
                id = 1, text = "File taxes",
                lastTouchedAt = LocalDateTime.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ),
            DriftItem(
                id = 2, text = "Go running",
                lastTouchedAt = LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )

        val result = service.fallbackFocus(items, emptyList())
        assertTrue("Should mention stale item", result.contains("File taxes"))
    }

    @Test
    fun `fallback focus prompts when only goals exist`() {
        val goals = listOf(
            DriftItem(id = 1, text = "Ship MVP", isGoal = true, goalHorizon = "month")
        )

        val result = service.fallbackFocus(emptyList(), goals)
        assertTrue("Should mention goals", result.contains("goals"))
    }

    @Test
    fun `fallback focus prompts to dump when empty`() {
        val result = service.fallbackFocus(emptyList(), emptyList())
        assertTrue(result.contains("dump") || result.contains("plate"))
    }

    @Test
    fun `fallback focus picks oldest item`() {
        val items = listOf(
            DriftItem(
                id = 1, text = "Recent task",
                lastTouchedAt = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            ),
            DriftItem(
                id = 2, text = "Old forgotten task",
                lastTouchedAt = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )

        val result = service.fallbackFocus(items, emptyList())
        assertTrue("Should pick oldest", result.contains("Old forgotten task"))
    }

    // --- Network fallback ---

    @Test
    fun `getDailyFocus falls back when backend unreachable`() = runBlocking {
        val items = listOf(
            DriftItem(
                id = 1, text = "Stale task",
                lastTouchedAt = LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
        )

        val result = service.getDailyFocus(items, emptyList())
        assertTrue("Should fallback gracefully", result.contains("Stale task"))
    }

    @Test
    fun `parseDump falls back when backend unreachable`() = runBlocking {
        val result = service.parseDump("file taxes, read a book")
        assertEquals(2, result.size)
    }
}
