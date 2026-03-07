# Keep Drift data models for JSON parsing
-keep class com.drift.app.data.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
