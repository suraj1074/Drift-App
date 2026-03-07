package com.drift.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class DriftDatabaseTest {

    private lateinit var db: DriftDatabase
    private lateinit var dao: DriftDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DriftDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.driftDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // --- Insert & Retrieve ---

    @Test
    fun insertItem_andRetrieve() = runBlocking {
        val item = DriftItem(text = "File taxes")
        dao.insert(item)

        val items = dao.getActiveItems()
        assertEquals(1, items.size)
        assertEquals("File taxes", items[0].text)
        assertEquals("active", items[0].status)
    }

    @Test
    fun insertMultipleItems_retrieveAll() = runBlocking {
        dao.insert(DriftItem(text = "Task 1"))
        dao.insert(DriftItem(text = "Task 2"))
        dao.insert(DriftItem(text = "Task 3"))

        val items = dao.getActiveItems()
        assertEquals(3, items.size)
    }

    // --- Goals vs Tasks ---

    @Test
    fun goalsAndTasks_separatedCorrectly() = runBlocking {
        dao.insert(DriftItem(text = "Run 3 times", isGoal = true, goalHorizon = "week"))
        dao.insert(DriftItem(text = "File taxes", isGoal = false))
        dao.insert(DriftItem(text = "Ship MVP", isGoal = true, goalHorizon = "month"))

        val goals = dao.getActiveGoals()
        val tasks = dao.getActiveTasks()

        assertEquals(2, goals.size)
        assertEquals(1, tasks.size)
        assertEquals("File taxes", tasks[0].text)
    }

    @Test
    fun goalHorizon_storedCorrectly() = runBlocking {
        dao.insert(DriftItem(text = "Weekly goal", isGoal = true, goalHorizon = "week"))
        dao.insert(DriftItem(text = "Monthly goal", isGoal = true, goalHorizon = "month"))

        val goals = dao.getActiveGoals()
        val weekly = goals.find { it.goalHorizon == "week" }
        val monthly = goals.find { it.goalHorizon == "month" }

        assertNotNull(weekly)
        assertNotNull(monthly)
    }

    // --- Status Updates ---

    @Test
    fun markItemDone_removesFromActive() = runBlocking {
        val id = dao.insert(DriftItem(text = "Finish book"))
        dao.updateStatus(id, "done")

        val items = dao.getActiveItems()
        assertTrue(items.isEmpty())
    }

    @Test
    fun markItemLetGo_removesFromActive() = runBlocking {
        val id = dao.insert(DriftItem(text = "Learn guitar"))
        dao.updateStatus(id, "let_go")

        val items = dao.getActiveItems()
        assertTrue(items.isEmpty())
    }

    @Test
    fun markItemPaused_removesFromActive() = runBlocking {
        val id = dao.insert(DriftItem(text = "Side project"))
        dao.updateStatus(id, "paused")

        val items = dao.getActiveItems()
        assertTrue(items.isEmpty())
    }

    @Test
    fun multipleStatusChanges_onlyActiveShown() = runBlocking {
        val id1 = dao.insert(DriftItem(text = "Task A"))
        val id2 = dao.insert(DriftItem(text = "Task B"))
        val id3 = dao.insert(DriftItem(text = "Task C"))

        dao.updateStatus(id1, "done")
        dao.updateStatus(id3, "let_go")

        val items = dao.getActiveItems()
        assertEquals(1, items.size)
        assertEquals("Task B", items[0].text)
    }

    // --- Touch / Drift Detection ---

    @Test
    fun touchItem_updatesLastTouchedAt() = runBlocking {
        val staleTime = LocalDateTime.now().minusDays(10)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val id = dao.insert(DriftItem(text = "Old task", lastTouchedAt = staleTime))

        dao.touch(id)

        val items = dao.getActiveItems()
        assertNotEquals(staleTime, items[0].lastTouchedAt)
    }

    @Test
    fun getDriftingItems_returnsOnlyStaleItems() = runBlocking {
        val fiveDaysAgo = LocalDateTime.now().minusDays(5)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDateTime.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        dao.insert(DriftItem(text = "Stale task", lastTouchedAt = fiveDaysAgo))
        dao.insert(DriftItem(text = "Fresh task", lastTouchedAt = now))

        // Threshold: items not touched in last 3 days
        val threshold = LocalDateTime.now().minusDays(3)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val drifting = dao.getDriftingItems(threshold)

        assertEquals(1, drifting.size)
        assertEquals("Stale task", drifting[0].text)
    }

    @Test
    fun getDriftingItems_excludesGoals() = runBlocking {
        val staleTime = LocalDateTime.now().minusDays(10)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        dao.insert(DriftItem(text = "Stale goal", isGoal = true, lastTouchedAt = staleTime))
        dao.insert(DriftItem(text = "Stale task", isGoal = false, lastTouchedAt = staleTime))

        val threshold = LocalDateTime.now().minusDays(3)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val drifting = dao.getDriftingItems(threshold)

        assertEquals(1, drifting.size)
        assertEquals("Stale task", drifting[0].text)
    }

    @Test
    fun getDriftingItems_excludesDoneItems() = runBlocking {
        val staleTime = LocalDateTime.now().minusDays(10)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val id = dao.insert(DriftItem(text = "Done stale task", lastTouchedAt = staleTime))
        dao.updateStatus(id, "done")

        val threshold = LocalDateTime.now().minusDays(3)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val drifting = dao.getDriftingItems(threshold)

        assertTrue(drifting.isEmpty())
    }

    // --- Ordering ---

    @Test
    fun activeItems_orderedByLastTouchedAscending() = runBlocking {
        val day1 = LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val day2 = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val day3 = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        dao.insert(DriftItem(text = "Middle", lastTouchedAt = day2))
        dao.insert(DriftItem(text = "Oldest", lastTouchedAt = day1))
        dao.insert(DriftItem(text = "Newest", lastTouchedAt = day3))

        val items = dao.getActiveItems()
        assertEquals("Oldest", items[0].text)
        assertEquals("Middle", items[1].text)
        assertEquals("Newest", items[2].text)
    }
}
