package eu.tutorials.stepscounter.databasehelpers

import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = ""
)

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

object Injection {
    private val instance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun instance(): FirebaseFirestore {
        return instance
    }
}
