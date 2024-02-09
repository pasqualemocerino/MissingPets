package com.macc.missingpets

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(auth: AuthViewModel, navController: NavController) {
    val user = auth.currentUser()
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
        Text(text = "Homepage")
        if (user != null) Text(text = "Welcome, ${user.displayName}")
        Button(onClick = {
            navController.navigate(Routes.CHATS)
        }) {
            Text("Chat list")
        }
        Button(onClick = {
            navController.navigate(Routes.CHAT + "/0" + "/ux0P38UTrdNgy6usK2OEZbrG7x32" + "/Chiara")
        }) {
            Text("New chat with Chiara")
        }
        Button(onClick = {
            // Handle log out logic
            auth.signOut()
            if (user != null) {
                Toast.makeText(
                    context,
                    "See you soon, ${user.displayName}!",
                    Toast.LENGTH_LONG
                ).show()
            }
            navController.navigate(Routes.LOGIN)
        }) {
            Text("Logout")
        }
    }
}