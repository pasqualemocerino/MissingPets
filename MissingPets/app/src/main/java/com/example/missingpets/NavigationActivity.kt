package com.example.missingpets

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.missingpets.ui.theme.MissingPetsTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.lang.Math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt

/*
        // QUESTA ATTIVITA' VA AVVIATA PASSANDO LA LATITUDINE E LONGITUDINE
        // COME PARAMETRI EXTRA ALL'INTENT, COSI':
        val intent = Intent(this, NavigationActivity::class.java)
        intent.putExtra("latitude", 41.9338758)     // latitudine della destinazione
        intent.putExtra("longitude", 12.4780725)    // longitudine della destinazione
        intent.putExtra("address", "via abc, Roma")
        startActivity(intent)
 */

class NavigationActivity : ComponentActivity(), SensorEventListener {

    // PER LA BUSSOLA
    private var sensorManager: SensorManager? = null
    private var magnetometer: Sensor? = null
    private var accelerometer: Sensor? = null
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    // Orientamento telefono rispetto al polo nord
    private var angleOrientation: Float = 0f

    // PER IL GPS
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    // DESTINAZIONE (fissa)
    private var goalLocation: Location = Location("")
    private var goalAddress: String = ""

    // POSIZIONE ATTUALE (si aggiorna col GPS)
    private var currentLocation: Location = Location("")

    // Angolo destinazione rispetto al polo nord
    private var bearing: Double = 0.0

    // Distanza tra posizione attuale e destinazione
    private var distance: Double = 0.0

    // Per mostrare la schermata di caricamento finche' il GPS non funziona
    private var waitingForGps = true

    // Per forzare il recompose della pagina quando i sensori ricevono nuovi dati
    private val recomposeToggleState: MutableState<Boolean> = mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prendi il goalLocation e l'indirizzo passati nell'intent
        val bundle = intent.extras
        goalLocation.latitude = bundle!!.getDouble("latitude")
        goalLocation.longitude = bundle!!.getDouble("longitude")
        goalAddress = bundle!!.getString("address").toString()

        /*
        // Temporaneo per vedere se funziona
        currentLocation.latitude = 41.934034
        currentLocation.longitude = 12.478201
        distance = computeDistance(currentLocation, goalLocation)
        bearing = computeBearing(currentLocation, goalLocation)
        */

        // Roba per la bussola:
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magnetometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        // Roba per il GPS:

        if (!checkPermission()) {       // chiedi permessi posizione
            requestPermission()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = 60000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        // Fai qualcosa con la nuova posizione ottenuta
                        updateCurrentLocation(location)
                        waitingForGps = false
                        manualRecompose()
                    }
                }
            }
        }
        startLocationUpdates()

        setContent {
            MissingPetsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleGoal()

                        Spacer(Modifier.fillMaxHeight(0.05f))

                        if (waitingForGps) {
                            LoadingScreen()
                        }
                        else {
                            DistanceText()

                            ArrowCanvas()

                            // Pulsante "Back"
                            Button(
                                onClick = { finish() }
                            ){
                                Text("Back")
                            }
                        }

                        // Temporaneo
                        //ButtonsCoordinates()
                    }

                    // Per far si' che il recompose forzato funzioni
                    LaunchedEffect(recomposeToggleState.value) {}
                }
            }
        }
    }

    // TEMPORANEO
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ButtonsCoordinates() {
        var lat:String by remember { mutableStateOf("41.934124") }
        var lon:String by remember { mutableStateOf("12.478321") }

        Column() {
            OutlinedTextField(
                value = lat,        // il valore mostrato nel campo
                onValueChange = {
                    // Prendi massimo descriptionMaxLength caratteri
                    lat = it
                },
                label = { Text("Latitude") }
            )
            OutlinedTextField(
                value = lon,        // il valore mostrato nel campo
                onValueChange = {
                    // Prendi massimo descriptionMaxLength caratteri
                    lon = it
                },
                label = { Text("Longitude") }
            )
            Button(
                onClick = {
                    goalLocation.latitude = lat.trim().toDouble()
                    goalLocation.longitude = lon.trim().toDouble()
                    updateCurrentLocation(currentLocation)
                    manualRecompose()
                }
            ) {
                Text("Set new goal")
            }
        }

    }

    @Composable
    fun LoadingScreen() {
        Column(
            //modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.Center
        ) {
            //Spacer(Modifier.fillMaxHeight(0.35f))   // spazio vuoto sopra la scritta

            Text(
                text = "Waiting for GPS...",
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

    @Composable
    fun TitleGoal() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Navigation",
                fontSize = 25.sp
            )
            Text(
                text = "Goal: " + goalAddress
            )
            Text(
                text = goalLocation.latitude.toString() + ", " + goalLocation.longitude.toString()
            )
        }
    }

    @Composable
    fun DistanceText() {
        val dist = distance.toFloat()
        var distanceString = ""
        if (dist < 1000) {
            distanceString = round(dist).toInt().toString() + " m"   // stampiamo i metri senza cifre decimali
        }
        else {
            distanceString = "%.2f".format(dist/1000) + " km"    // per stampare i km con 2 cifre decimali
        }

        Column() {
            Text(
                text = "Distance: " + distanceString
            )
        }
    }

    @Composable
    fun ArrowCanvas() {
        val arrow = BitmapFactory.decodeStream(this.assets.open("arrow.png"))
        val arrowBitmap = arrow!!.asImageBitmap()

        Image(
            bitmap = arrowBitmap,
            contentDescription = "arrow",
            modifier = Modifier.rotate(-angleOrientation + bearing.toFloat()),
            contentScale = ContentScale.None
        )

    }



    /**********************************      BUSSOLA      **********************************/


    // Callback per i sensori
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == magnetometer) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        } else if (event.sensor == accelerometer) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        }
        if (lastAccelerometerSet && lastMagnetometerSet) {
            SensorManager.getRotationMatrix(
                rotationMatrix,
                null,
                lastAccelerometer,
                lastMagnetometer
            )
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthInRadians = orientation[0]
            val azimuthInDegrees = (Math.toDegrees(azimuthInRadians.toDouble()) + 360).toFloat() % 360

            if (abs(azimuthInDegrees - angleOrientation) > 5) {
                angleOrientation = azimuthInDegrees
                manualRecompose()
            }
        }
    }

    // Registra listener per gli eventi dei sensori
    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this, magnetometer)
        sensorManager!!.unregisterListener(this, accelerometer)
    }


    // questa serve per forza se no il compilatore si lamenta
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}




    /**********************************      GPS      **********************************/


    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }


    // Callback per il GPS
    private fun updateCurrentLocation(newLocation: Location) {

        currentLocation = newLocation

        distance = computeDistance(currentLocation, goalLocation)
        bearing = computeBearing(currentLocation, goalLocation)
    }


    // Distanza in metri considerando la superficie sferica della terra
    fun computeDistance(startPoint: Location, endPoint:Location): Double {
        val R = 6371e3                          // raggio della terra in metri

        val phi1 = Math.toRadians(startPoint.latitude)
        //val lambda1 = Math.toRadians(startPoint.longitude)

        val phi2 = Math.toRadians(endPoint.latitude)
        //val lambda2 = Math.toRadians(endPoint.longitude)

        val deltaphi = Math.toRadians(endPoint.latitude-startPoint.latitude)
        val deltalambda = Math.toRadians(endPoint.longitude-startPoint.longitude)

        val a = sin(deltaphi/2) * sin(deltaphi/2) + cos(phi1) * cos(phi2) * sin(deltalambda/2) * sin(deltalambda/2)
        val c = 2 * atan2(Math.sqrt(a), sqrt(1-a))

        val distance = R * c  // in metres
        return distance
    }

    // Bearing = angolo tra l'asse che congiunge startPoint e polo nord
    // e asse che congiunge startPoint e endPoint
    fun computeBearing(startPoint: Location, endPoint:Location): Double {
        // (phi1,lambda1) is the start point, (phi2,lambda2) the end point

        val phi1 = Math.toRadians(startPoint.latitude)
        val lambda1 = Math.toRadians(startPoint.longitude)

        val phi2 = Math.toRadians(endPoint.latitude)
        val lambda2 = Math.toRadians(endPoint.longitude)

        val y = sin(lambda2-lambda1) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1)* cos(phi2) * cos(lambda2-lambda1)

        val theta = atan2(y, x)
        val bearing = (Math.toDegrees(theta) + 360) % 360       // in degrees

        return bearing
    }


    // Per fare refresh dei composable
    fun manualRecompose() {
        recomposeToggleState.value = !recomposeToggleState.value
    }



    // Per chiedere i permessi del GPS
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }
}