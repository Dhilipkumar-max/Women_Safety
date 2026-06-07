package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "guardians")
data class Guardian(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val status: String // "Active", "Pending"
)

@Entity(tableName = "emergency_logs")
data class EmergencyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Fall Detected", "Manual SOS", "Voice Trigger", "Abnormal Heart Rate"
    val score: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: String // "Active Alert", "False Alarm", "Cancelled"
)

@Entity(tableName = "recordings")
data class Recording(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val duration: String, // e.g. "0:45", "1:20"
    val timestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0
)

@Entity(tableName = "sensor_telemetry")
data class SensorTelemetry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val heartRate: Int,
    val spo2: Int,
    val temperature: Float,
    val accX: Float,
    val accY: Float,
    val accZ: Float,
    val movement: String,
    val batteryLevel: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs (Data Access Objects) ---

@Dao
interface SafetyDao {
    // Guardians
    @Query("SELECT * FROM guardians ORDER BY name ASC")
    fun getAllGuardians(): Flow<List<Guardian>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuardian(guardian: Guardian)

    @Delete
    suspend fun deleteGuardian(guardian: Guardian)

    @Query("DELETE FROM guardians")
    suspend fun clearGuardians()

    // Emergency Logs
    @Query("SELECT * FROM emergency_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EmergencyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: EmergencyLog)

    @Query("DELETE FROM emergency_logs")
    suspend fun clearLogs()

    // Audio Recordings
    @Query("SELECT * FROM recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<Recording>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: Recording)

    @Delete
    suspend fun deleteRecording(recording: Recording)

    @Query("DELETE FROM recordings")
    suspend fun clearRecordings()

    // Sensor Telemetry
    @Query("SELECT * FROM sensor_telemetry ORDER BY timestamp DESC")
    fun getAllTelemetry(): Flow<List<SensorTelemetry>>

    @Query("SELECT * FROM sensor_telemetry WHERE username = :username ORDER BY timestamp DESC")
    fun getTelemetryForUser(username: String): Flow<List<SensorTelemetry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: SensorTelemetry)

    @Query("DELETE FROM sensor_telemetry")
    suspend fun clearTelemetry()
}

// --- AppDatabase ---

@Database(
    entities = [Guardian::class, EmergencyLog::class, Recording::class, SensorTelemetry::class],
    version = 2,
    exportSchema = false
)
abstract class SafetyDatabase : RoomDatabase() {
    abstract fun safetyDao(): SafetyDao

    companion object {
        @Volatile
        private var INSTANCE: SafetyDatabase? = null

        fun getDatabase(context: Context): SafetyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafetyDatabase::class.java,
                    "safety_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Unified Repository Pattern ---

class SafetyRepository(private val safetyDao: SafetyDao) {
    val allGuardians: Flow<List<Guardian>> = safetyDao.getAllGuardians()
    val allLogs: Flow<List<EmergencyLog>> = safetyDao.getAllLogs()
    val allRecordings: Flow<List<Recording>> = safetyDao.getAllRecordings()

    suspend fun insertGuardian(guardian: Guardian) {
        safetyDao.insertGuardian(guardian)
    }

    suspend fun deleteGuardian(guardian: Guardian) {
        safetyDao.deleteGuardian(guardian)
    }

    suspend fun clearGuardians() {
        safetyDao.clearGuardians()
    }

    suspend fun insertLog(log: EmergencyLog) {
        safetyDao.insertLog(log)
    }

    suspend fun clearLogs() {
        safetyDao.clearLogs()
    }

    suspend fun insertRecording(recording: Recording) {
        safetyDao.insertRecording(recording)
    }

    suspend fun deleteRecording(recording: Recording) {
        safetyDao.deleteRecording(recording)
    }

    suspend fun clearRecordings() {
        safetyDao.clearRecordings()
    }

    val allTelemetry: Flow<List<SensorTelemetry>> = safetyDao.getAllTelemetry()

    fun getTelemetryForUser(username: String): Flow<List<SensorTelemetry>> {
        return safetyDao.getTelemetryForUser(username)
    }

    suspend fun insertTelemetry(telemetry: SensorTelemetry) {
        safetyDao.insertTelemetry(telemetry)
    }

    suspend fun clearTelemetry() {
        safetyDao.clearTelemetry()
    }
}
