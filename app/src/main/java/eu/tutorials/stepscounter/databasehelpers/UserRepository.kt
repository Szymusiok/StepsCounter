package eu.tutorials.stepscounter.databasehelpers

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import eu.tutorials.stepscounter.model.Workout
import eu.tutorials.stepscounter.model.toFirestorePath
import eu.tutorials.stepscounter.model.toLatLngList
import kotlinx.coroutines.tasks.await
import eu.tutorials.stepscounter.model.toEntity
import eu.tutorials.stepscounter.model.toModel

// Handles communication with Firebase and local database
class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val database: AppDatabase
){
    // Save user data to Firestore
    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.email).set(user).await()
    }

    suspend fun getUserData(email: String): Result<User> = try {
        val snapshot = firestore.collection("users").document(email).get().await()
        val user = snapshot.toObject(User::class.java)
        if (user != null) Result.Success(user) else Result.Error(Exception("User not found"))
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<Boolean> =
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(firstName, lastName, email)
            saveUserToFirestore(user = user)
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun login(email: String, password: String): Result<Boolean> =
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun saveWorkout(userEmail: String, workout: Workout): Result<Boolean> {
        database.workoutDao().insert(workout.toEntity())
        return try {
            val data = hashMapOf(
                "path" to workout.path.toFirestorePath(),
                "distanceMeters" to workout.distanceMeters,
                "steps" to workout.steps,
                "calories" to workout.calories,
                "durationMs" to workout.durationMs,
                "timestamp" to workout.timestamp
            )
            firestore.collection("users")
                .document(userEmail)
                .collection("workouts")
                .add(data)
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getWorkouts(userEmail: String): List<Workout> {
        val local = database.workoutDao().getAll().map { it.toModel() }
        return try {
            val snapshot = firestore.collection("users")
                .document(userEmail)
                .collection("workouts")
                .get()
                .await()

            val remote = snapshot.documents.mapNotNull { doc ->
                val path = (doc["path"] as? List<Map<String, Double>>)?.toLatLngList() ?: emptyList()
                Workout(
                    id = doc.id,
                    path = path,
                    distanceMeters = doc["distanceMeters"] as? Double ?: 0.0,
                    steps = (doc["steps"] as? Long)?.toInt() ?: 0,
                    calories = (doc["calories"] as? Long)?.toInt() ?: 0,
                    durationMs = doc["durationMs"] as? Long ?: 0L,
                    timestamp = doc["timestamp"] as? Timestamp ?: Timestamp.now()
                )
            }
            remote.forEach { database.workoutDao().insert(it.toEntity()) }
            remote
        } catch (e: Exception) {
            local
        }
    }

    suspend fun getWorkoutById(workoutId: String): Workout? {
        val local = database.workoutDao().getById(workoutId)?.toModel()
        return try {
            val userEmail = auth.currentUser?.email ?: return local
            val snapshot = firestore.collection("users")
                .document(userEmail)
                .collection("workouts")
                .document(workoutId)
                .get()
                .await()

            val doc = snapshot.data ?: return local
            val path = (doc["path"] as? List<Map<String, Double>>)?.toLatLngList() ?: emptyList()

            val workout = Workout(
                id = workoutId,
                path = path,
                distanceMeters = doc["distanceMeters"] as? Double ?: 0.0,
                steps = (doc["steps"] as? Long)?.toInt() ?: 0,
                calories = (doc["calories"] as? Long)?.toInt() ?: 0,
                durationMs = doc["durationMs"] as? Long ?: 0L,
                timestamp = doc["timestamp"] as? Timestamp ?: Timestamp.now()
            )
            database.workoutDao().insert(workout.toEntity())
            workout
        } catch (e: Exception) {
            local
        }
    }

    suspend fun deleteWorkout(userEmail: String, workoutId: String): Result<Boolean> {
        database.workoutDao().deleteById(workoutId)
        return try {
            firestore.collection("users")
                .document(userEmail)
                .collection("workouts")
                .document(workoutId)
                .delete()
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}