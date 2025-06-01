package eu.tutorials.stepscounter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.tutorials.stepscounter.KdamThmorPro
import eu.tutorials.stepscounter.databasehelpers.Result
import eu.tutorials.stepscounter.ui.theme.BORDOWY
import eu.tutorials.stepscounter.ui.theme.JASNY_KREMOWY
import eu.tutorials.stepscounter.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val authResult by authViewModel.authResult.observeAsState(initial = null)
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authResult) {
        if (authResult is Result.Error) {
            errorMessage = (authResult as Result.Error).exception.localizedMessage ?: "Sign-up failed."
            showErrorDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SignUpCard(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            passwordVisible = passwordVisible,
            onEmailChange = { email = it },
            onPasswordChange = { password = it },
            onFirstNameChange = { firstName = it },
            onLastNameChange = { lastName = it },
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            onSignUp = {
                authViewModel.signUp(email, password, firstName, lastName)
                email = ""
                password = ""
                firstName = ""
                lastName = ""
            },
            onNavigateToLogin = onNavigateToLogin
        )

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Sign Up Error", fontFamily = KdamThmorPro) },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK", fontFamily = KdamThmorPro)
                    }
                }
            )
        }
    }
}

@Composable
private fun SignUpCard(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BORDOWY,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            AuthField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email",
                icon = Icons.Default.Email
            )

            AuthField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                icon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onToggleVisibility = onTogglePasswordVisibility
            )

            AuthField(
                value = firstName,
                onValueChange = onFirstNameChange,
                placeholder = "First name",
                icon = Icons.Default.Person
            )

            AuthField(
                value = lastName,
                onValueChange = onLastNameChange,
                placeholder = "Last name",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignUp,
                colors = ButtonDefaults.buttonColors(containerColor = BORDOWY),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SIGN UP", fontSize = 20.sp, fontFamily = KdamThmorPro, color = JASNY_KREMOWY)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Do you have an account? Sign in",
                modifier = Modifier
                    .clickable(onClick = onNavigateToLogin)
                    .padding(8.dp),
                fontFamily = KdamThmorPro
            )
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = BORDOWY) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = BORDOWY) },
        trailingIcon = if (isPassword && onToggleVisibility != null) {
            {
                val toggleIcon = if (passwordVisible) Icons.Default.Info else Icons.Default.Lock
                IconButton(onClick = onToggleVisibility) {
                    Icon(toggleIcon, contentDescription = null, tint = BORDOWY)
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}
