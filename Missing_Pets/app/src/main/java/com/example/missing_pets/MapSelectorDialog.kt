package com.example.missing_pets

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.api.IMapController
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import rememberMapViewWithLifecycle

class MapSelectorDialog {

    lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var requestingLocationUpdates = false

    lateinit var map: MapView
    var startPoint: GeoPoint = GeoPoint(41.933835, 12.478106)
    lateinit var mapController: IMapController
    var marker: Marker? = null

    lateinit var context: Context

    var startLocation: GeoPoint? = null

    @Composable
    fun MapSelector(
        context: Context
    ) {
        /*
        val appPerms = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
        )
        activityResultLauncher.launch(appPerms)
        */

        this.context = context
        MapView(context, startLocation, ::initMap)
    }



    @SuppressLint("MissingPermission")
    @Composable
    fun MapView(
        context: Context,
        startLocation: GeoPoint?,
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

        /*
        if (!requestingLocationUpdates) {
            requestingLocationUpdates = true
            //startLocationUpdates()
        }
        */

        mapController = map.controller
        mapController.setZoom(18.5)

        if (this.startLocation != null) {
            //Log.d("MAPPA", "using startLocation")
            startPoint = this.startLocation!!
            mapController.setCenter(this.startLocation)
        }
        else {
            //Log.d("MAPPA", "using initLocation")
            initLocation(context)
        }

        marker = null
        getPositionMarker(context).position = startPoint

        //map.invalidate()
    }

    private fun initLocation(context: Context) { //call in create
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        readLastKnownLocation(context)
    }

    @SuppressLint("MissingPermission") //permission are checked before
    fun readLastKnownLocation(context: Context) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { updateLocation(context, it) }
            }
    }

    fun updateLocation(context: Context, newLocation: Location) {
        lastLocation = newLocation

        //GUI, MAP TODO
        //binding.tvLat.setText(newLocation.latitude.toString())
        //binding.tvLon.setText(newLocation.longitude.toString())

        //var currentPoint: GeoPoint = GeoPoint(newLocation.latitude, newLocation.longitude);

        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude
        mapController.setCenter(startPoint)
        getPositionMarker(context).position = startPoint

        //map.invalidate()
    }


    fun updateLocation(context: Context, newLocation: GeoPoint) {
        lastLocation?.longitude = newLocation.longitude
        lastLocation?.latitude = newLocation.latitude

        startPoint.longitude = newLocation.longitude
        startPoint.latitude = newLocation.latitude

        // Sposta la mappa in modo che il punto cliccato sia al centro
        //mapController.setCenter(startPoint)       // sposta immediatamente
        mapController.animateTo(startPoint)         // sposta in modo fluido

        // Posiziona il marker sul punto cliccato
        getPositionMarker(context).position = startPoint

        //map.invalidate()
    }


    private fun getPositionMarker(context: Context): Marker { //Singelton
        if (marker == null) {
            marker = Marker(map)
            marker!!.title = "Here I am"
            marker!!.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker!!.icon = ContextCompat.getDrawable(context, R.drawable.ic_position);
            map.overlays.add(marker)
        }
        return marker!!
    }

    fun getPosition(): GeoPoint {
        return startPoint
    }

    fun getPositionAsString(): String {
        return "(" + startPoint.latitude.toString() + ", " + startPoint.longitude.toString() + ")"
    }
}



class MyMapEventsReceiver : MapEventsReceiver {

    private var callback : (context: Context, position: GeoPoint) -> Unit
    private lateinit var context: Context

    constructor(context:Context, callback: (context: Context, position: GeoPoint) -> Unit) : super() {
        this.context = context
        this.callback = callback
    }
    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
        Log.d("posizione", p.toString())
        callback(context, p)
        return true
    }

    override fun longPressHelper(p: GeoPoint): Boolean {
        //Log.d("posizione", p.toString())
        callback(context, p)
        return true
    }
}