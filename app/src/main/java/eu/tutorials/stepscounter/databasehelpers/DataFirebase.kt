package eu.tutorials.stepscounter.databasehelpers

import com.google.firebase.firestore.FirebaseFirestore

// Simple helpers for Firebase calls
data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
)

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// Singleton to get Firestore instance
object Injection {
    private val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun instance(): FirebaseFirestore {
        return instance
    }
}
