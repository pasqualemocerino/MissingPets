package com.example.missing_pets

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

// Parametri globali per tutto il progetto
const val PET_TYPE_DOG: Int = 0
const val PET_TYPE_CAT: Int = 1

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);

        // Gli faccio caricare subito l'attivita' CreatePost
        startActivity(Intent(this, SeePostsActivity::class.java));

        //startActivity(Intent(this, MapActivity::class.java));
    }
}

