package com.example.missingpets


import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import com.example.missingpets.Routes


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(auth: AuthViewModel, navController: NavController) {

    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }

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
        Spacer(modifier = Modifier.height(32.dp))
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
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            // Handle login logic here
            auth.signIn(
                email = emailState.value,
                password = passwordState.value
            )
            val user = auth.currentUser()
            if (user != null) {
                Toast.makeText(
                    context,
                    "Welcome, ${user.displayName}",
                    Toast.LENGTH_LONG
                ).show()
                // Avvia activity principale
                val userId = auth.currentUser()?.uid.toString()
                val username = auth.currentUser()?.displayName.toString()
                val intent = Intent(context, AppActivity::class.java )
                intent.putExtra("user_id", userId)     // user_id che ha fatto il login
                intent.putExtra("username", username)     // username che ha fatto il login
                context.startActivity(intent)
            }
            //navController.navigate(Routes.HOME)
        }) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
            Text("Don't have an account? Register here")
        }
    }
}