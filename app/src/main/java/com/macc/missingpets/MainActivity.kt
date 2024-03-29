package com.macc.missingpets

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.macc.missingpets.ui.theme.MissingPetsTheme


class MainActivity : ComponentActivity() {
    private lateinit var authViewModel:AuthViewModel

    /*private var chatList:MutableList<Chat> = mutableListOf(
        Chat(0, "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale","Ciao", formatTime(Date(System.currentTimeMillis() + 1)), false)
    )

    private var messageList:MutableList<ChatMessage> = mutableListOf(
        ChatMessage(0, "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale", "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "Ciao Chiara", formatTime(Date(System.currentTimeMillis())), false),
        ChatMessage(1, "ux0P38UTrdNgy6usK2OEZbrG7x32", "Chiara", "4qQUbahchPT9oqT0VY1PRvvwSu92", "Pasquale", "Ciao", formatTime(Date(System.currentTimeMillis() + 1)), false)
    )
    */

    @RequiresApi(Build.VERSION_CODES.O)
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
                    val startDestination =
                        if (authViewModel.isAuthenticated) { Routes.HOME }
                        else { Routes.LOGIN }
                    NavHost(
                        navController,
                        startDestination = startDestination
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
                            ChatsScreen(authViewModel, navController)
                        }
                        composable(
                            route = Routes.CHAT + "/{chatId}/{chatNameId}/{chatName}",
                            arguments = listOf(
                                navArgument("chatId") {
                                    type = NavType.IntType
                                },
                                navArgument("chatNameId") {
                                    type = NavType.StringType
                                },
                                navArgument("chatName") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val chatId = backStackEntry.arguments?.getInt("chatId")
                            val chatNameId = backStackEntry.arguments?.getString("chatNameId")
                            val chatName = backStackEntry.arguments?.getString("chatName")
                            if (chatId != null && chatNameId != null && chatName != null) ChatScreen(authViewModel, chatId, chatNameId, chatName, navController)
                            else Log.e("MainActivity", "null chatNameId or chatName")
                        }
                    }
                }
            }
        }
    }
}