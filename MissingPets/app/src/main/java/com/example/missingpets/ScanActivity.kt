package com.example.missingpets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ScanActivity : AppCompatActivity() {
    private lateinit var cameraOpenId: Button
    private lateinit var clickImageId: ImageView
    private lateinit var confirmId: Button
    private lateinit var messageId: TextView

    // Pagine
    private lateinit var pageContent: TableLayout
    private lateinit var loadingScreen: TableLayout

    // Foto che viene scattata
    private var photo: Bitmap? = null

    // Attiviamo il pulsante "confirm" solo quando questi due sono entrambi veri
    private var loadedPhoto = false
    private var loadedGPS = false


    // Per il GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
        private const val pic_id = 123
        private const val show_result_activity_id = 45
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_camera)

        cameraOpenId = findViewById(R.id.camera_button)
        clickImageId = findViewById(R.id.click_image)
        confirmId = findViewById(R.id.confirm_button)
        confirmId.isEnabled = false // lo attiviamo dopo che scatti la foto e hai la posizione col gps
        messageId = findViewById(R.id.textViewMessage)

        pageContent = findViewById(R.id.page_content)
        loadingScreen = findViewById(R.id.loading_screen)
        loadingScreen.isVisible = false

        // Aggiungi listener al pulsante "camera"
        cameraOpenId.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }


        // Aggiungi listener al pulsante "confirm"
        confirmId.setOnClickListener {
            // Nascondi pagina, mostra schermata di caricamento
            pageContent.isVisible = false
            loadingScreen.isVisible = true

            CoroutineScope(Dispatchers.IO).launch {
                runBlocking{
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDateTime.now().format(formatter)
                    val position = getPositionAsString(lastLocation!!)
                    val res = PostsHandler.getBestMatchingPosts(0, date, position, photo!!, this@ScanActivity)
                    Log.d("RISPOSTA MATCH", res.toString())
                }
                // Apri attivita' per mostrarti il risultato del match
                //startActivity(Intent(this@ScanActivity, MatchResultActivity::class.java))
                intent = Intent(this@ScanActivity, MatchResultActivity::class.java)
                startActivityForResult(intent, show_result_activity_id)
            }
        }


        // Per il GPS:

        if (!checkGPSPermission())
            requestGPSPermission()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {

                        // Aggiorna posizione
                        lastLocation = location
                        loadedGPS = true

                        // Togli messaggio "waiting for GPS..."
                        messageId.isVisible = false

                        if (loadedPhoto) {
                            confirmId.isEnabled = true // se hai sia foto che gps attiva il pulsante
                        }
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, pic_id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Se torni dall'attivita' che scatta la foto
        if (requestCode == pic_id && resultCode == RESULT_OK && data != null) {
            photo = data.extras?.get("data") as Bitmap?
            clickImageId.setImageBitmap(photo)

            // Imposta pulsante per inviare la foto al server
            loadedPhoto = true
            if (loadedGPS) {
                confirmId.isEnabled = true  // se hai sia foto che gps attiva il pulsante
            }
        }

        // Se torni dall'attivita' che ti mostra i risultati del match
        if (requestCode == show_result_activity_id) {
            finish()    // termina questa attivita'
        }
    }



    fun getPositionAsString(location:Location): String {
        // Tagliamo le coordinate a 6 cifre decimali per evitare problemi nel server
        // (6 cifre decimali corrispondono ad una precisione di 1 metro)
        var lat = "%.6f".format(Locale.ENGLISH, location.latitude)
        var lon = "%.6f".format(Locale.ENGLISH, location.longitude)
        val str = lat + "," + lon
        return str
    }


    // Permessi camera
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    // Permessi GPS
    private fun checkGPSPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }
    private fun requestGPSPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }
}