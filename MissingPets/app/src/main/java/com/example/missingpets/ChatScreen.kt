package com.example.missingpets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.navigation.NavController


//----------------------------------------CHAT PAGE---------------------------------------
@Composable
fun ChatScreen(navController: NavController) {
    Column {
        Logo(navController = navController, username = "Chiara")
    }
}


