package com.example.missing_pets

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import kotlinx.coroutines.runBlocking

class SeePostsActivity: ComponentActivity() {

    // per prendere la lista di post
    var postsHandler = PostsHandler()
    private var postsList : MutableList<Post> = ArrayList<Post>()

    // misure dello schermo, per disegnare bene gli elementi (inizializzate dentro onCreate)
    var screenWidthDp = 0
    var screenHeightDp = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // prendi misure dello schermo
        val configuration: Configuration = this.getResources().getConfiguration()
        screenWidthDp = configuration.screenWidthDp
        screenHeightDp = configuration.screenHeightDp

        // prendi la lista dei post
        runBlocking {
            postsList = postsHandler.getAll()
        }
        Log.d("posts", postsList.toString())

        setContent {
            Test_Caricamento_AnnuncioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                        // Lista dei post
                        Column(
                            Modifier
                                .heightIn(0.dp, (screenHeightDp * 0.8f).dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)   // per lasciare spazio tra un elemento e l'altro
                        ) {
                            // aggiungi un elemento per ogni post
                            for (i:Post in postsList) {
                                PostElement(i)
                            }
                        }

                        // Pulsante per andare alla pagina dove creare un nuovo post
                        Button(onClick = {
                            startActivity(Intent(this@SeePostsActivity, CreatePostActivity::class.java));
                        }) {
                            Text("Create New Post")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun PostElement(post: Post) {

        // DA CAMBIARE in modo che non sia hard coded
        val photoURL = "https://maccproject2024.pythonanywhere.com/photo?post_id=" + post.post_id.toString()

        Row (
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            val imageModifier = Modifier
                .width((screenWidthDp/3.4).dp)
                .height((screenWidthDp/2.5).dp)
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
                    text = "Location: " + post.position
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
            }
        }


    }
}