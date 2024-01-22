package com.example.missing_pets

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import kotlinx.coroutines.runBlocking



class CreatePostActivity : ComponentActivity() {

    private var user_id = 0     // verra' preso dinamicamente dall'account che ha fatto il log in
    private var pet_type = 0    // verra' scelto da un dropdown menu, con opzioni associate a valori statici
    private var date = ""
    private var position = ""
    private var description = ""

    // parametri per mettere i valori corretti
    private var descriptionMaxLength = 255          // perche' nel database il campo description e' varchar(255)


    // per inviare il nuovo post
    var postsHandler = PostsHandler()

    // per il calendario
    var calendar = Calendar.getInstance()
    var current_year = calendar.get(Calendar.YEAR);
    var current_month = calendar.get(Calendar.MONTH);
    var current_day = calendar.get(Calendar.DAY_OF_MONTH);


    // Funzione chiamata quando l'activity viene creata
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContent {
            Test_Caricamento_AnnuncioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PageContent()
                }
            }
        }
    }

    /*
    Ogni funzione annotata con "Composable" e' una componente di Jetpack Compose.
    Non restituisce niente, semplicemente descrive che aspetto deve avere questa
    parte dell'interfaccia.

    remember = store the value just in case recompose is called
    mutablestate = store the value and in case I update value trigger, recompose for all elements using this data
    */


    // COMPOSABLE PRINCIPALE
    @Composable
    fun PageContent() {

        // per mostrare messaggi di errore quando i valori messi non sono validi
        val showDateError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val showPositionError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val showDescriptionError: MutableState<Boolean> = remember { mutableStateOf(false) }

        Column(
            Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)   // per lasciare spazio tra un elemento e l'altro
        ) {
            // Titolo
            Text(
                text = "Create a new post",
                fontSize = 30.sp
            )
            // Form
            Form(showDateError, showPositionError, showDescriptionError)
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Form(showDateError: MutableState<Boolean>, showPositionError: MutableState<Boolean>, showDescriptionError: MutableState<Boolean>,modifier: Modifier = Modifier) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            // Campo in cui scegliere la data
            DateField(showDateError)

            // Campo in cui scegliere la posizione
            PositionField(showPositionError)

            // Campo in cui scegliere la descrizione
            DescriptionField()

            // Pulsante per creare il post
            Button(onClick = {
                // Controlla se i campi sono validi
                if (validateFields(showDateError, showPositionError, showDescriptionError)) {

                    // Crea nuovo post (da sostituire con una cosa non-blocking che mostra una schermata di caricamento)
                    runBlocking{
                        val res = postsHandler.createPost(user_id, pet_type, date, position, description)
                    }
                    // Ritorna alla pagina con tutti i post
                    startActivity(Intent(this@CreatePostActivity, SeePostsActivity::class.java));
                }
            }) {
                Text("Create Post")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DateField(showDateError: MutableState<Boolean>) {
        //var text by remember { mutableStateOf(date) }

        Column() {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Date: "
                )
                //val date = remember { mutableStateOf("") }
                val datePickerDialog = DatePickerDialog(
                    this@CreatePostActivity,
                    {_, year : Int, month: Int, day: Int ->
                        val m = month+1     // perche' i mesi (solo i mesi) partono da 0 per qualche motivo
                        var month_string = m.toString()
                        if (m < 10)
                            month_string = "0" + month_string   // perche' serve '01' invece di '1'
                        var day_string = day.toString()
                        if (day < 10)
                            day_string = "0" + day_string
                        date = "$year-$month_string-$day_string"
                        Log.d("date", date)
                        showDateError.value = false                   // nascondi messaggio di errore
                    }, current_year, current_month, current_day
                )
                Button(onClick = {
                    datePickerDialog.show()
                }) {
                    Text(text = "Choose")
                }
            }
            // Messaggio di errore
            if (showDateError.value) {
                Text(
                    text = "Please choose a date.",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PositionField(showPositionError: MutableState<Boolean>) {
        var text by remember { mutableStateOf(position) }

        Column() {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Position: "
                )
                OutlinedTextField(
                    value = text,        // il valore mostrato nel campo
                    onValueChange = {
                        text = it
                        position = it
                    },
                    label = { Text("Position") }
                )
            }
            // Messaggio di errore
            if (showPositionError.value) {
                Text(
                    text = "Please choose a position.",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }

    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DescriptionField() {
        var text by remember { mutableStateOf(description) }
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Description: "
            )
            OutlinedTextField(
                value = text,        // il valore mostrato nel campo
                onValueChange = {
                    // Prendi massimo descriptionMaxLength caratteri
                    text = it.take(descriptionMaxLength)
                    description = text
                },
                label = { Text("Description") }
            )
        }
    }



    fun validateFields(showDateError: MutableState<Boolean>, showPositionError: MutableState<Boolean>, showDescriptionError: MutableState<Boolean>): Boolean {
        if (date != "" && position != "") {
            if (description == "") {
                description = " "         // perche' non so se inviare una stringa vuota al DB da' problemi
            }
            showPositionError.value = false                   // nascondi messaggio di errore
            return true
        }

        if (date == "") {
            showDateError.value = true;
        }
        if (position == "") {
            showPositionError.value = true;
        }

        return false
    }


}