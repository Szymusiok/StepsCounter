package eu.tutorials.stepscounter

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    videoUri: Uri,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
){
    // Observe signup result from view model
    val authResult by authViewModel.authResult.observeAsState(initial = null)
    var showErrorDialog by remember { mutableStateOf(false)}
    var errorMessage by remember { mutableStateOf("")}

    val context = LocalContext.current
    PutBackground(context = context, videoUri = videoUri)

    LaunchedEffect(authResult) {
        if(authResult is Result.Error){
            errorMessage = (authResult as Result.Error).exception.localizedMessage
                ?: "An error occurred during sign-up."
            showErrorDialog = true
        }
    }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            onClick = {
                authViewModel.signUp(email, password, firstName, lastName)
                email = ""
                password = ""
                firstName = ""
                lastName = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Sign Up")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Already have an account? Sign in.",
            modifier = Modifier.clickable { onNavigateToLogin()}
            )
    }

    if(showErrorDialog){
        AlertDialog(
            onDismissRequest = { showErrorDialog = false},
            title = { Text("Sign Up Error")},
            text = { Text(errorMessage)},
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false}) {
                    Text("OK")
                }
            }
        )
    }
}