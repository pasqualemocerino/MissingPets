package com.macc.missingpets

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.macc.missingpets.ui.theme.MissingPetsTheme


class MainActivity : ComponentActivity() {
    private lateinit var authViewModel:AuthViewModel

    private var list: List<ChatMessage> = listOf(
        ChatMessage(0, "user0", "user1", "Hello", System.currentTimeMillis(), false),
        ChatMessage(1, "user1", "user0", "Hello to you", System.currentTimeMillis() + 1, true)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MissingPetsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    authViewModel = viewModel()
                    val navController = rememberNavController()
                    NavHost(
                        navController,
                        startDestination = Routes.LOGIN
                    ) {
                        composable(Routes.LOGIN) {
                            LoginScreen(authViewModel, navController)
                        }
                        composable(Routes.REGISTER) {
                            RegistrationScreen(authViewModel, navController)
                        }
                        composable(Routes.HOME) {
                            HomeScreen(authViewModel, navController)
                        }
                        composable(Routes.CHATS) {
                            ChatsScreen(authViewModel, list, navController)
                        }
                        composable(Routes.CHAT) {
                            ChatScreen(authViewModel, "chatName", list, navController)
                        }
                    }
                }
            }
        }
    }
}

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