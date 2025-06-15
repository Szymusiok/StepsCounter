package eu.tutorials.stepscounter.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.stepscounter.databasehelpers.Injection
import eu.tutorials.stepscounter.databasehelpers.Result
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import eu.tutorials.stepscounter.databasehelpers.AppDatabase
import kotlinx.coroutines.launch

class AuthViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Injection.instance(),
        AppDatabase.getInstance(app)
    )

    private val _authResult = MutableLiveData<Result<Boolean>>()
    val authResult: LiveData<Result<Boolean>> = _authResult

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = userRepository.signUp(email, password, firstName, lastName)
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authResult.value = userRepository.login(email, password)
            _isLoading.value = false
        }
    }

    private fun execute(block: suspend () -> Result<Boolean>) {
        viewModelScope.launch {
            _authResult.value = block()
        }
    }
}
