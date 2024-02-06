package com.example.missingpets

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.missingpets.ui.theme.MissingPetsTheme
import com.macc.missingpets.Routes
import java.util.*


// Parametri globali per tutto il progetto
const val PET_TYPE_DOG: String = "Dog"
const val PET_TYPE_CAT: String = "Cat"


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MissingPetsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //to show the bottom navigation bar
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavigationBar(navController = navController) }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Routes.HOME,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Routes.HOME) { HomeScreen(navController = navController) }
                            // queste due sotto le ho tolte perche' sono activities a parte, non routes
                            //composable("scan") { ScanScreen(navController = navController) }
                            //composable("add") { AddScreen(navController = navController) }
                            composable(Routes.CHATS) { ChatScreen(navController = navController) }
                            composable(Routes.PROFILE) { ProfileScreen(navController = navController) }
                        }
                    }

                    }
                }
            }
        }
    }


                //------------------------------------------
    //HOMEPAGE
    /*
    @Composable
    fun HomeScreen(navController: NavController) {
        Column {
            Logo(navController = navController, username = "Chiara")
            /*val context = LocalContext.current
            Button(onClick = {
                context.startActivity(Intent(context, SeePostsActivity::class.java))
            }) {
                Text(text = "Show latest announcements")
            }*/
        }
    }
    */



    //----------------------------------------------------------------------------

    //SCAN
    @Composable
    fun ScanScreen(navController: NavController) {
        Column {
            Logo(navController = navController, username = "Chiara")
            val context = LocalContext.current
            Button(onClick = {
                context.startActivity(Intent(context, ScanActivity::class.java))
            }) {
                Text(text = "Start Camera")
            }

        }
    }


    //----------------------------------------LOGO--------------------------------------------------
    @Composable
    fun Logo(navController: NavController, username: String) {
        //to insert the logo and the "Hi, User" on the same line with space between
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(100.dp)
        ) {
            //to insert the logo image and define its dimensions
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            //to insert the profile icon + "Hi,User"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "User Icon")
                Text(text = "Hi, $username", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }
    }


//-------------------------------------BOTTOM NAVBAR-----------------------------------------------

    //Define the icons and the icons text for each option of the bottom navbar
    enum class BottomNavItem(val route: String, val icon: ImageVector) {
        Home(Routes.HOME, Icons.Default.Home),
        Scan("Scan", Icons.Default.Search),
        Add("Add", Icons.Default.Add),
        Chats(Routes.CHATS, Icons.Default.Send),
        Profile(Routes.PROFILE, Icons.Default.Person)
    }

    //define the bottom navbar
    @Composable
    fun BottomNavigationBar(navController: NavController) {
        BottomNavigation(
            modifier = Modifier.background(MaterialTheme.colorScheme.primary)
        ) {
            val context = LocalContext.current
            BottomNavItem.values().forEach { item ->
                BottomNavigationItem(
                    selected = navController.currentDestination?.route == item.route,
                    onClick = {
                        when (item) {
                            BottomNavItem.Home -> {
                                navController.navigate(Routes.HOME)
                            }

                            BottomNavItem.Scan -> {
                                context.startActivity(Intent(context, ScanActivity::class.java ))
                            }

                            BottomNavItem.Add -> {
                                context.startActivity(Intent(context, CreatePostActivity::class.java ))
                                }

                            BottomNavItem.Chats -> {
                                navController.navigate(Routes.CHATS)
                            }

                            BottomNavItem.Profile -> {
                                navController.navigate(Routes.PROFILE)
                            }
                        }
                    },
                    icon = {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    label = { Text(item.route, color = MaterialTheme.colorScheme.onPrimary) }
                )
            }
        }
    }




