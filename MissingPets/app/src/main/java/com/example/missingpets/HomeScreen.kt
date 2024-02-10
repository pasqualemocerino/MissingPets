package com.example.missingpets

import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.*
import java.util.ArrayList

// E' brutto averla come variabile globale ma funziona solo cosi' :(( poi magari cerco una soluz migliore
var postsList : MutableList<Post> = ArrayList<Post>()

@Composable
fun HomeScreen(navController: NavController) {

    //val user = auth.currentUser()
    val context = LocalContext.current


    Column(
        Modifier
            .background(Color(230, 230, 230, 255))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)   // per lasciare spazio tra un elemento e l'altro
    ) {

        // Titolo
        Text(
            text = "Posts",
            fontSize = 30.sp
        )

        // Per mostrare la schermata di caricamento
        var loading = remember { mutableStateOf(true) }

        // Prendi la lista dei post in una coroutine (asincrono)
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                CoroutineScope(Dispatchers.IO).launch {
                    runBlocking {
                        postsList = PostsHandler.getPostsList()
                    }
                    loading.value = false
                    Log.d("DONE!", "obtained posts from server")
                }
            }
        }

        // Lista dei post o schermata di caricamento, a seconda del valore di loading
        PostsListOrLoading(loading, navController)
    }
}


@Composable
fun PostsListOrLoading(loading:MutableState<Boolean>, navController: NavController) {

    // misure dello schermo, per disegnare bene gli elementi
    val context = LocalContext.current
    val configuration: Configuration = context.getResources().getConfiguration()
    var screenWidthDp = configuration.screenWidthDp
    var screenHeightDp = configuration.screenHeightDp

    val heightOfSection = (screenHeightDp * 0.8f).dp

    // Schermata di caricamento
    if (loading.value) {
        Box(
            modifier = Modifier.height(heightOfSection)
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp)
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                //trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
        return
    }

    // Lista dei post
    Column(
        Modifier
            //.heightIn(0.dp, heightOfSection)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)   // per lasciare spazio tra un elemento e l'altro
    ) {
        // aggiungi un elemento per ogni post
        for (i:Post in postsList) {
            PostElement(i, navController)
        }
    }
}

@Composable
fun PostElement(post: Post, navController: NavController) {

    // misure dello schermo, per disegnare bene gli elementi
    val context = LocalContext.current
    val configuration: Configuration = context.getResources().getConfiguration()
    var screenWidthDp = configuration.screenWidthDp
    var screenHeightDp = configuration.screenHeightDp

    // DA CAMBIARE in modo che non sia hard coded
    val photoURL = "https://maccproject2024.pythonanywhere.com/photo?post_id=" + post.post_id.toString()

    Row (
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        val imageModifier = Modifier
            .width((screenWidthDp / 3.4).dp)
            .height((screenWidthDp / 2.5).dp)
            .border(BorderStroke(1.dp, Color.Black))
            .background(Color.White)

        Image(
            painter = rememberAsyncImagePainter(photoURL),
            contentDescription = "pet picture :3",
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )

        Column(
            Modifier
                .background(Color.White)
                .fillMaxWidth()
        ) {
            Text(
                text = "Post " + post.post_id,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Date: " + post.date
            )
            Text(
                text = "Location: " + post.address
            )
            Text(
                text = "Pet type: " + post.pet_type
            )
            Text(
                text = "Description: " + post.description
            )
            Text(
                text = "User: " + post.user_id
            )
            NavigationButton(post.position, post.address)

            ChatButton(navController, post.user_id, "persona")
        }
    }

}

@Composable
fun ChatButton(navController:NavController, post_user_id:String, post_username:String) {
    Button(onClick = {
        navController.navigate(Routes.CHAT + "/0" + "/" + post_user_id + "/" + post_username)
    }) {
        Text("Chat")
    }
}

@Composable
fun NavigationButton(position:String, address:String) {
    val context = LocalContext.current

    // Prendi coordinate dalla posizione
    val coords = position.split(",")
    val lat = coords[0].trim().toDouble()
    val lon = coords[1].trim().toDouble()

    val intent = Intent(context, NavigationActivity::class.java)
    intent.putExtra("latitude", lat)     // latitudine della destinazione
    intent.putExtra("longitude", lon)    // longitudine della destinazione
    intent.putExtra("address", address)  // indirizzo destinazione, per farlo stampare

    Button(
        onClick = {
            context.startActivity(intent)
        }
    ){
        Text("Navigate")
    }

}

