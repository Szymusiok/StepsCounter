package eu.tutorials.stepscounter.databasehelpers

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import eu.tutorials.stepscounter.databasehelpers.WorkoutEntity

// Data access object for workouts
@Dao
interface WorkoutDao {

    // All workouts, newest first
    @Query("SELECT * FROM workouts ORDER BY timestamp DESC")
    suspend fun getAll(): List<WorkoutEntity>

    // Single workout by id
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getById(id: String): WorkoutEntity?

    // Save or update a workout
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutEntity)

    // Remove a workout
    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM workouts WHERE synced = 0")
    suspend fun getUnsynced(): List<WorkoutEntity>

    @Query("UPDATE workouts SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}