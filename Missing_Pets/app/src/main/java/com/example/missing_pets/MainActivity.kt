package com.example.missing_pets

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import kotlinx.coroutines.runBlocking
import org.osmdroid.config.Configuration
import java.io.File

// Parametri globali per tutto il progetto
const val PET_TYPE_DOG: String = "Dog"
const val PET_TYPE_CAT: String = "Cat"

class MainActivity : ComponentActivity() {

    var mapSelectorDialog = MapSelectorDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // per risolvere il problema della cache di osmdroid (non funziona comunque)
        //fixCacheError(ContextWrapper(this))

        // Attivita' SeePostsActivity
        startActivity(Intent(this, SeePostsActivity::class.java));

        // Attivita' MapActivity
        //startActivity(Intent(this, MapActivity::class.java));

        /*
        setContent {
            Test_Caricamento_AnnuncioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    mapDialogButton()
                }
            }
        }
        */
    }


    @Composable
    fun mapDialogButton() {
        val context: Context = LocalContext.current


        // Dialog state Manager
        val dialogState: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        // Code to Show and Dismiss Dialog
        if (dialogState.value) {
            AlertDialog(
                onDismissRequest = { dialogState.value = false },
                title = {
                    Text(text = "Choose the position")
                },
                text = {
                    Box(
                        modifier = Modifier
                            .width(300.dp)
                            .height(330.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .border(BorderStroke(4.dp, Color.White))
                    ) {
                        //mapSelectorDialog.reloadMarker()
                        mapSelectorDialog.MapSelector(context)
                    }

                },
                confirmButton = {
                    Button(
                        onClick = {
                            val pos = mapSelectorDialog.getPositionAsString()
                            Log.d("pos", pos)
                        }
                    ) {
                        Text(
                            text = "Confirm",
                            fontSize = 16.sp
                            )
                    }
                }
                //properties = DialogProperties(
                //    dismissOnBackPress = false,
                //    dismissOnClickOutside = false
                //)
            )
            //dvm.doSomethingMore()
        } else {
            //Toast.makeText(ctx, "Dialog Closed", Toast.LENGTH_SHORT).show()
            //dvm.doSomething()
        }

        // Show UI - In this case, we will be using just to show button
        Row(
            modifier = Modifier
                .fillMaxHeight(1f)
                .fillMaxWidth(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                dialogState.value = true
            }) {
                Text(text = "Show Dialog", fontSize = 22.sp)
            }
        }
    }

    private fun fixCacheError(contextWrapper: ContextWrapper) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        val root = contextWrapper.filesDir
        val osmdroidBasePath = File(root, "osmdroid")
        osmdroidBasePath.mkdirs()
        Configuration.getInstance().setOsmdroidBasePath(osmdroidBasePath)
    }
}

