package eu.tutorials.stepscounter.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import eu.tutorials.stepscounter.databasehelpers.Injection
import eu.tutorials.stepscounter.databasehelpers.Result
import eu.tutorials.stepscounter.databasehelpers.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Injection.instance()
    )
) : ViewModel() {

    private val _authResult = MutableLiveData<Result<Boolean>>()
    val authResult: LiveData<Result<Boolean>> = _authResult

    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        execute { userRepository.signUp(email, password, firstName, lastName) }
    }

    fun login(email: String, password: String) {
        execute { userRepository.login(email, password) }
    }

    private fun execute(block: suspend () -> Result<Boolean>) {
        viewModelScope.launch {
            _authResult.value = block()
        }
    }
}
