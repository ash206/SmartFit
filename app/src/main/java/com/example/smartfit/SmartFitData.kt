package com.example.smartfit

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// --- ROOM DATABASE (Local Storage) ---

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "steps", "workout", "food"
    val value: Int,
    val calories: Int,
    val date: Long, // timestamp
    val notes: String
)

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY date DESC")
    fun getAllActivities(): Flow<List<ActivityLog>>

    @Insert
    suspend fun insert(activity: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAll()
}

@Database(entities = [ActivityLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartfit_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

// --- DATASTORE (Settings) ---

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }
}