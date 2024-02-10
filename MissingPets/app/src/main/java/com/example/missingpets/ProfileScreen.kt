package com.example.missingpets

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.inappmessaging.model.Button
import kotlinx.coroutines.runBlocking
import java.util.ArrayList


//-----------------------------------------PROFILE--------------------------------------------------
@Composable
fun ProfileScreen(user_id:String, username:String, navController: NavController) {

    val context = LocalContext.current
    val configuration: Configuration = context.getResources().getConfiguration()
    var screenWidthDp = configuration.screenWidthDp
    var screenHeightDp = configuration.screenHeightDp

    var profilePostsList : MutableList<Post> = ArrayList<Post>()

    // Prendi i post dal server
    runBlocking {
        profilePostsList = PostsHandler.getUserPostList(user_id)
    }


    Column {
        Logo(navController = navController, username = username)

        //AboutYouSection(user = User("Chiara", "chiara@prova.it"))
        //OwnPost(postsHandler = PostsHandler())

        // Pulsante logout
        Button(onClick = {
            // Handle log out logic
            authViewModel!!.signOut()
            if (username != null) {
                Toast.makeText(
                    context,
                    "See you soon, ${username}!",
                    Toast.LENGTH_LONG
                ).show()
            }
            navController.navigate(Routes.EXIT)
            //finish()
        }) {
            Text("Logout")
        }

        // I TUOI POST
        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)   // per lasciare spazio tra un elemento e l'altro
        ) {

            // Titolo
            Text(
                text = "Your posts",
                fontSize = 18.sp
            )

            // Lista dei post
            Column(
                Modifier
                    .heightIn(0.dp, (screenHeightDp * 0.75f).dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)   // per lasciare spazio tra un elemento e l'altro
            ) {
                // aggiungi un elemento per ogni post
                for (i:Post in profilePostsList) {
                    ProfilePostElement(i)
                }
            }
        }
    }
}

//Definition of the user whos credentials will be taken from the database
data class User(
    val username: String,
    val email: String,
)

//Definition of the about you section with the summary of all the info about the user
@Composable
fun AboutYouSection(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "About You", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Username: ${user.username}")
        Text("Email: ${user.email}")
    }
}

/*@Composable
fun OwnPost(postsHandler : PostsHandler) {
    val firstPost = remember { postsHandler.getFirstPost() }
    var postElement: SeePostsActivity = SeePostsActivity()
    Column {
        if (firstPost != null) {
            postElement.PostElement(post = firstPost)
        } else {
            Text(text = "You don't have announcements")
        }
    }
}*/


@Composable
fun ProfilePostElement(post: Post) {

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
        }
    }

}
