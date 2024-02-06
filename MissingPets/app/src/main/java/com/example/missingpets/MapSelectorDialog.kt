package com.example.missingpets

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import rememberMapViewWithLifecycle
import java.io.File
import java.util.Locale


// Source:
// https://github.com/osmdroid/osmdroid/wiki/Incorporating-Location-Updates-Into-an-OSMDroid-Application-(Kotlin)
// https://github.com/kwesiRutledge/MapOsmdroidKotlinExample/blob/master/app/src/main/java/com/um/feri/cs/pora/mapkotlinexample/MainActivity.kt

class MapSelectorDialog {

    lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var requestingLocationUpdates = false
    private var locationCallback: LocationCallback
    private var locationRequest: LocationRequest

    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(41.933835, 12.478106)
    lateinit var mapController: IMapController
    var marker: Marker? = null

    lateinit var context: Context

    var startLocation: GeoPoint? = null

    constructor(context: Context) {

        this.context = context

        // Imposta il path della cache
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


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
                        //UI updates
                        updateLocation(location)
                    }
                }
            }
        }

    }

    fun setCachePath() {    // questa non funziona
        Log.d("CACHE OSMDROID", "configuring cache path")
        val osmConf = Configuration.getInstance()
        val basePath = File(context.cacheDir.absolutePath, "osmdroid")
        osmConf.osmdroidBasePath = basePath
        val tileCache = File(osmConf.osmdroidBasePath.absolutePath, "tile")
        osmConf.osmdroidTileCache = tileCache

    }

    @Composable
    fun MapSelector() {

        val appPerms = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            //Manifest.permission.ACCESS_NETWORK_STATE,
            //Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.INTERNET
        )
        //activityResultLauncher.launch(appPerms)

        MapView(context, ::initMap)
    }



    @SuppressLint("MissingPermission")
    @Composable
    fun MapView(
        context: Context,
        onLoad: ((context: Context, map: MapView) -> Unit)
    ) {
        val mapViewState = rememberMapViewWithLifecycle()

        AndroidView(
            { mapViewState },
            modifier = Modifier.fillMaxSize()
        ) { mapView -> onLoad.invoke(context, mapView) }
    }

    private fun initMap(context: Context, mapView: MapView) {

        Log.d("INIT MAPPA", "")

        // Inizializza parametri
        map = mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setTilesScaledToDpi(true)       // se no si vede tutto piccolo
        map.setMultiTouchControls(true)


        // Aggiungi listener
        var mReceiver = MyMapEventsReceiver(context, ::updateLocation)
        var overlayEvents = MapEventsOverlay(context, mReceiver)
        map.getOverlays().add(overlayEvents)


        mapController = map.controller
        mapController.setZoom(18.5)

        if (this.startLocation != null) {
            //Log.d("MAPPA", "using startLocation")
            startPoint = this.startLocation!!
            mapController.setCenter(this.startLocation)

            if (requestingLocationUpdates) stopLocationUpdates()
            requestingLocationUpdates = false
        }
        else {
            //Log.d("MAPPA", "using initLocation")

            if (!requestingLocationUpdates) {
                requestingLocationUpdates = true
                startLocationUpdates()
            }
            initLocation()
        }

        marker = null
        getPositionMarker().position = startPoint

        //map.invalidate()
    }

    private fun initLocation() { //call in create
        readLastKnownLocation()
    }

    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    updateLocation(it)
                    if (requestingLocationUpdates) stopLocationUpdates()   // quando hai ottenuto la posizione smetti di cercarla
                    requestingLocationUpdates = false
                }
            }
    }

    fun updateLocation(newLocation: Location) {
        lastLocation = newLocation

        //GUI, MAP TODO
        //binding.tvLat.setText(newLocation.latitude.toString())
        //binding.tvLon.setText(newLocation.longitude.toString())

        //var currentPoint: GeoPoint = GeoPoint(newLocation.latitude, newLocation.longitude);

        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        mapController.setCenter(startPoint)
        getPositionMarker().position = startPoint


        if (requestingLocationUpdates) stopLocationUpdates()   // quando hai ottenuto la posizione smetti di cercarla
        requestingLocationUpdates = false

        //map.invalidate()
    }


    fun updateLocation(newLocation: GeoPoint) {
        lastLocation?.longitude = newLocation.longitude
        lastLocation?.latitude = newLocation.latitude

        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude

        // Sposta la mappa in modo che il punto cliccato sia al centro
        //mapController.setCenter(startPoint)       // sposta immediatamente
        mapController.animateTo(startPoint)         // sposta in modo fluido

        // Posiziona il marker sul punto cliccato
        getPositionMarker().position = startPoint

        //map.invalidate()
    }


    private fun getPositionMarker(): Marker { //Singelton
        if (marker == null) {
            marker = Marker(map)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(context, R.drawable.ic_position);
            map.overlays.add(marker)
        }
        return marker!!
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() { //onResume

        LocationServices.getFusedLocationProviderClient(context)
            .requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun updatePositionWithGPS() {
        //readLastKnownLocation()

        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            startLocationUpdates()
        }
    }


    fun getPosition(): GeoPoint {
        return startPoint
    }

    fun getPositionAsString(): String {
        // Tagliamo le coordinate a 8 cifre decimali per evitare problemi nel server
        var lat = "%.8f".format(Locale.ENGLISH, startPoint.latitude)
        var lon = "%.8f".format(Locale.ENGLISH, startPoint.longitude)
        val str = lat + "," + lon
        //Log.d("coordinate", str)
        return str
    }
}



class MyMapEventsReceiver : MapEventsReceiver {

    private var callback : (position: GeoPoint) -> Unit
    private lateinit var context: Context

    constructor(context:Context, callback: (position: GeoPoint) -> Unit) : super() {
        this.context = context
        this.callback = callback
    }
    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
        //Log.d("posizione", p.toString())
        callback(p)
        return true
    }

    override fun longPressHelper(p: GeoPoint): Boolean {
        //Log.d("posizione", p.toString())
        callback(p)
        return true
    }
}