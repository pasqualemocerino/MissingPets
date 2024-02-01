package com.example.missing_pets

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.io.File
import java.util.Dictionary
import java.util.Locale


class CreatePostActivity : ComponentActivity() {

    private var user_id = 0     // verra' preso dinamicamente dall'account che ha fatto il log in

    private lateinit var photo: AppCompatImageView
    private var petName = ""
    private var photoURI: Uri? = null
    private var date = ""
    private var pet_type = PET_TYPE_DOG
    private var position = ""
    private var description = ""

    // parametri per mettere i valori corretti
    private val descriptionMaxLength = 255          // perche' nel database il campo description e' varchar(255)
    private val petNameMaxLength = 20

    // per gestire la mappa
    private lateinit var mapSelectorDialog : MapSelectorDialog

    // per inviare il nuovo post
    private var postsHandler = PostsHandler()

    // per mostrare la schermata di caricamento
    private lateinit var loading: MutableState<Boolean>

    // per il calendario
    private var calendar = Calendar.getInstance()
    private var current_year = calendar.get(Calendar.YEAR)
    private var current_month = calendar.get(Calendar.MONTH)
    private var current_day = calendar.get(Calendar.DAY_OF_MONTH)

    // indici errori
    val petNameErrorIndex = 0
    val photoErrorIndex = 1
    val dateErrorIndex = 2
    val positionErrorIndex = 3
    val descriptionErrorIndex = 4



    // Funzione chiamata quando l'activity viene creata
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapSelectorDialog = MapSelectorDialog(this)

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



    // COMPOSABLE PRINCIPALE
    @Composable
    fun PageContent() {

        // per mostrare messaggi di errore quando i valori messi non sono validi
        val petNameError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val photoError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val dateError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val positionError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val descriptionError: MutableState<Boolean> = remember { mutableStateOf(false) }
        val missingFieldErrors : Array<MutableState<Boolean>> = arrayOf(petNameError, photoError, dateError, positionError, descriptionError)

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

            // Per mostrare la schermata di caricamento quando invii il post
            loading = remember { mutableStateOf(false) }

            if (!loading.value) {
                // Form
                Form(missingFieldErrors)
            }
            else {
                LoadingScreen()
            }
        }
    }

    @Composable
    fun LoadingScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.fillMaxHeight(0.35f))   // spazio vuoto sopra la scritta

            Text(
                text = "Creating post...",
                fontSize = 20.sp,
                modifier = Modifier.padding(20.dp)
                //textAlign = Alignment.Center
            )
            CircularProgressIndicator(
                modifier = Modifier
                    .width(64.dp),
                //.align(Alignment.Center),
                color = MaterialTheme.colorScheme.secondary,
                //trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Form(missingFieldErrors: Array<MutableState<Boolean>>, modifier: Modifier = Modifier) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            PetNameField(missingFieldErrors[petNameErrorIndex])

            PhotoField(missingFieldErrors[photoErrorIndex])

            // Campo in cui scegliere la data
            DateField(missingFieldErrors[dateErrorIndex])

            PetTypeField()

            // Campo in cui scegliere la posizione
            PositionField(missingFieldErrors[positionErrorIndex])

            // Campo in cui scegliere la descrizione
            DescriptionField()

            // Pulsante per creare il post
            Button(onClick = {
                // Controlla se i campi sono validi
                if (validateFields(missingFieldErrors)) {

                    // Mostra schermata di caricamento
                    loading.value = true

                    CoroutineScope(Dispatchers.IO).launch {
                        runBlocking {
                            val res = postsHandler.createPost(user_id, petName, pet_type, date, position, description, getPath(photoURI))
                            Log.d("Server response", res.toString())
                            // TODO: gestire errore server in base al valore di res
                        }
                        finish()
                    }

                    /*
                    // Crea nuovo post (da sostituire con una cosa non-blocking che mostra una schermata di caricamento)
                    runBlocking{
                        val res = postsHandler.createPost(user_id, petName, pet_type, date, position, description, getPath(photoURI))
                    }
                    // Ritorna alla pagina con tutti i post
                    //startActivity(Intent(this@CreatePostActivity, SeePostsActivity::class.java))
                    finish()
                    */
                }
            }) {
                Text("Create Post")
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PetNameField(showPetNameError: MutableState<Boolean>) {
        var text by remember { mutableStateOf("") }

        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Pet name: "
                )
                OutlinedTextField(
                    value = text,        // il valore mostrato nel campo
                    onValueChange = {
                        // Prendi massimo descriptionMaxLength caratteri
                        text = it.take(petNameMaxLength)
                        petName = text
                        showPetNameError.value = false     // nascondi messaggio di errore
                    },
                    label = { Text("Pet name") }
                )
            }
            // Messaggio di errore
            if (showPetNameError.value) {
                Text(
                    text = "Please add your pet's name.",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }
    }



    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 41
    fun checkPermissionToReadExternalStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result: Int = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            return result == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    @Throws(Exception::class)
    fun requestPermissionToReadExternalStorage() {
        try {
            ActivityCompat.requestPermissions(
                (this as Activity?)!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
            Log.d("REQUESTING PERMISSION", "aaa")
        } catch (e: Exception) {
            Log.d("ERRORE PERMESSI", ":(((")
            e.printStackTrace()
            throw e
        }
    }



    @Composable
    fun PhotoField(showPhotoError: MutableState<Boolean>) {
        var text by remember { mutableStateOf("") }

        Column {

            photo = AppCompatImageView(this@CreatePostActivity)

            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Photo: "
                )
                val pickImg = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                Button(onClick = {
                    if (!checkPermissionToReadExternalStorage())
                        requestPermissionToReadExternalStorage()
                    changeImage.launch(pickImg)

                    showPhotoError.value = false     // nascondi messaggio di errore
                }) {
                    Text(text = "Choose")
                }
            }
            // Messaggio di errore
            if (showPhotoError.value) {
                Text(
                    text = "Please choose a photo.",
                    fontSize = 14.sp,
                    color = Color.Red
                )
            }
        }
    }


    @Composable
    fun DateField(showDateError: MutableState<Boolean>) {
        var text by remember { mutableStateOf("") }

        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Date: "
                )
                // Campo dove appare la data selezionata
                Text(
                    text = text
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
                        text = date
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
    fun PetTypeField() {
        val context = LocalContext.current
        var expanded by remember { mutableStateOf(false) }
        val options = arrayOf(PET_TYPE_DOG, PET_TYPE_CAT)
        var selectedText by remember { mutableStateOf(options[0]) }

        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "Pet category: "
            )

            Box(
                modifier = Modifier
                    //.fillMaxWidth()
                    .padding(32.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = {
                        expanded = !expanded
                    }
                ) {
                    TextField(
                        value = selectedText,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        options.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    selectedText = item
                                    expanded = false
                                    pet_type = item
                                    //Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PositionField(showPositionError: MutableState<Boolean>) {
        var text by remember { mutableStateOf(position) }

        // Dialog state Manager
        val dialogState: MutableState<Boolean> = remember {mutableStateOf(false)}

        Column {
            Row (
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Position: "
                )
                // Campo dove appare la posizione selezionata
                Text(
                    text = text
                )

                // Pulsante per aprire la mappa
                Button(onClick = {
                    dialogState.value = true
                }) {
                    Text(
                        text = "Choose",
                        //fontSize = 22.sp
                    )
                }
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
                            mapSelectorDialog.MapSelector()
                        }

                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Richiedi i permessi per la posizione
                                requestLocationPermissions()

                                // passagli l'ultima posizione come startLocation
                                mapSelectorDialog.startLocation = mapSelectorDialog.getPosition().clone()

                                position = mapSelectorDialog.getPositionAsString()
                                Log.d("pos", position)
                                text = "selected"   // se no la vera posizione viene troppo lunga

                                showPositionError.value = false     // nascondi messaggio di errore
                                dialogState.value = false           // chiudi dialog
                            }
                        ) {
                            Text(
                                text = "Ok",
                                fontSize = 16.sp
                            )
                        }
                    },dismissButton = {
                        Button(
                            onClick = {
                                mapSelectorDialog.updatePositionWithGPS()
                            }
                        ) {
                            Text("Use GPS")
                        }
                    }

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



    fun validateFields(missingFieldErrors: Array<MutableState<Boolean>>): Boolean {

        // Controlla se c'e' il nome
        missingFieldErrors[petNameErrorIndex].value = (petName == "")

        // Controlla se c'e' la foto
        missingFieldErrors[photoErrorIndex].value = (photoURI == null)

        // Controlla se c'e' la foto
        missingFieldErrors[dateErrorIndex].value = (date == "")

        // Controlla se c'e' la posizione
        missingFieldErrors[positionErrorIndex].value = (position == "")


        if (petName != "" && photoURI != null && date != "" && position != "") {
            if (description == "") {
                description = " "         // perche' non so se inviare una stringa vuota al DB da' problemi
            }
            return true
        }
        return false
    }



    private val changeImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data
                val imgUri = data?.data
                photo.setImageURI(imgUri)
                if (imgUri != null) {
                    photoURI = imgUri
                }
            }
        }


    fun getPath(uri: Uri?): String {
        val projection = arrayOf<String>(MediaStore.MediaColumns.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }


    fun requestLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.ACCESS_FINE_LOCATION), 1);

        } else{
            //   getLocationfromYourDevice();
        }
    }



    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1
            )
            false
        } else {
            true
        }
    }

}