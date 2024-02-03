package com.example.missingpets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


//-----------------------------------------PROFILE--------------------------------------------------
@Composable
fun ProfileScreen(navController: NavController) {
    Column {
        Logo(navController = navController, username = "Chiara")
        AboutYouSection(user = User("Chiara", "chiara@prova.it"))
        //OwnPost(postsHandler = PostsHandler())
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
