package com.example.missing_pets


import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.missing_pets.ui.theme.Test_Caricamento_AnnuncioTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import rememberMapViewWithLifecycle


// Source:
// https://gist.github.com/ArnyminerZ/418683e3ef43ccf1268f9f62940441b1#file-maplifecycle-kt

class MapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        setContent {
            Test_Caricamento_AnnuncioTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapView2()
                }
            }
        }
    }

    @Composable
    fun MapView2() {

        val mapView = rememberMapViewWithLifecycle()

        MapViewContainer(
            mapView = mapView,
            latitude = -37.0,
            longitude = 145.0
        )


    }

    @Composable
    private fun MapViewContainer(mapView: MapView, latitude: Double, longitude: Double) {
        LaunchedEffect(mapView) {
            /*
            val map = mapView.awaitMap()
            map.moveCamera(
                CameraUpdateFactory.newLatLng(LatLng(latitude, longitude))
            )
            */
        }
        AndroidView({ mapView })
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun MapView(
        modifier: Modifier = Modifier,
        onLoad: ((map: MapView) -> Unit)? = null
    ) {
        val mapViewState = rememberMapViewWithLifecycle()

        /*
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val lastKnownLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnownLoc != null) {
            val longitude = (lastKnownLoc.longitude * 1000000).toInt()
            val latitude = (lastKnownLoc.latitude * 1000000).toInt()
            val location = GeoPoint(latitude, longitude)
        }
        */


        AndroidView(
            { mapViewState },
            modifier
        ) { mapView -> onLoad?.invoke(mapView) }
    }
}