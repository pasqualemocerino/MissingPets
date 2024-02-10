package com.example.missingpets


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.missingpets.Routes

fun isEmailValid(email: String): Boolean {
    // Regex for email validation
    val emailRegex = Regex("""^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""")
    return email.matches(emailRegex)
}

fun isPasswordValid(password: String): Boolean {
    // Password validation rules
    val minLength = 8
    val containsUppercase = Regex("""[A-Z]""")
    val containsLowercase = Regex("""[a-z]""")
    val containsDigit = Regex("""\d""")

    return password.length >= minLength &&
            password.contains(containsUppercase) &&
            password.contains(containsLowercase) &&
            password.contains(containsDigit)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(auth: AuthViewModel, navController: NavController) {
    val usernameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    //val passwordVisibility =  remember { mutableStateOf(false) }
    //val confirmPasswordVisibility = remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Missing Pets",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(text = "Registration")
        Spacer(modifier = Modifier.height(32.dp))
        TextField(
            value = usernameState.value,
            onValueChange = { usernameState.value = it },
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
            //visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPasswordState.value,
            onValueChange = { confirmPasswordState.value = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
            //visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("The password must be at least 8 characters long, with at least " +
                "one uppercase character, one lowercase character and one digit."
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            var validSignUp = true
            val username = usernameState.value
            val email = emailState.value
            val password = passwordState.value
            val confirmPassword = confirmPasswordState.value

            var errorMessage = ""

            if (username.isEmpty()) {
                validSignUp = false
                if (errorMessage.isEmpty()) errorMessage = "Empty username"
            }
            if (!isEmailValid(email)) {
                validSignUp = false
                if (errorMessage.isEmpty()) errorMessage = "Invalid e-mail"
            }
            if (!isPasswordValid(password)) {
                validSignUp = false
                if (errorMessage.isEmpty()) errorMessage = "Invalid password, check password policy"
            }
            if (confirmPassword != password) {
                validSignUp = false
                if (errorMessage.isEmpty()) errorMessage = "Not matching passwords"
            }
            if (!validSignUp) {
                Toast.makeText(
                    context,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            else {
                // After checking e-mail and password, it is possible to sign up
                auth.signUp(
                    username = username,
                    email = email,
                    password = password
                )
                Toast.makeText(
                    context,
                    "You signed up correctly.",
                    Toast.LENGTH_LONG
                ).show()
                // Login redirection
                navController.navigate(Routes.LOGIN)
            }
        }) {
            Text("Register")
        }
        TextButton(onClick = { navController.navigate(Routes.LOGIN) }) {
            Text("Already have an account? Login here")
        }
    }
}