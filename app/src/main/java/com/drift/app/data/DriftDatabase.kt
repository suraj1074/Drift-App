package com.drift.app.data

import android.content.Context
import androidx.room.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// --- Entities ---

@Entity(tableName = "items")
data class DriftItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val category: String? = null,       // "task", "goal", "idea", "obligation"
    val status: String = "active",      // "active", "paused", "done", "let_go"
    val createdAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val lastTouchedAt: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    val goalHorizon: String? = null,    // "week", "month", "quarter", null for tasks
    val isGoal: Boolean = false
)

// --- DAO ---

@Dao
interface DriftDao {
    @Query("SELECT * FROM items WHERE status = 'active' ORDER BY lastTouchedAt ASC")
    suspend fun getActiveItems(): List<DriftItem>

    @Query("SELECT * FROM items WHERE isGoal = 1 AND status = 'active'")
    suspend fun getActiveGoals(): List<DriftItem>

    @Query("SELECT * FROM items WHERE isGoal = 0 AND status = 'active' ORDER BY lastTouchedAt ASC")
    suspend fun getActiveTasks(): List<DriftItem>

    @Query("SELECT * FROM items WHERE status = 'active' AND isGoal = 0 AND lastTouchedAt < :threshold ORDER BY lastTouchedAt ASC")
    suspend fun getDriftingItems(threshold: String): List<DriftItem>

    @Insert
    suspend fun insert(item: DriftItem): Long

    @Update
    suspend fun update(item: DriftItem)

    @Query("UPDATE items SET lastTouchedAt = :now WHERE id = :id")
    suspend fun touch(id: Long, now: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))

    @Query("UPDATE items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}

// --- Database ---

@Database(entities = [DriftItem::class], version = 1)
abstract class DriftDatabase : RoomDatabase() {
    abstract fun driftDao(): DriftDao

    companion object {
        @Volatile private var INSTANCE: DriftDatabase? = null

        fun get(context: Context): DriftDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, DriftDatabase::class.java, "drift.db")
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
