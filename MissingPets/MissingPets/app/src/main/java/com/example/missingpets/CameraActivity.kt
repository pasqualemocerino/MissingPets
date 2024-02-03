package com.example.missingpets

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import android.Manifest


class CameraActivity: ComponentActivity() {

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
        if (result) {
            // La foto è stata scattata con successo
            // Esegui l'operazione di matching con il tuo database
            // Puoi passare l'URI dell'immagine catturata o gestire l'immagine direttamente qui
            // Successivamente, puoi navigare a un'altra schermata se necessario
        } else {
            // C'è stato un errore nel processo di scatto
            Toast.makeText(this, "Error taking picture", Toast.LENGTH_SHORT).show()
        }
    }

    // Funzione per avviare l'attività della fotocamera
    private fun startCameraActivity(navController: NavController) {
        if (hasCameraPermission()) {
            // Se l'autorizzazione alla fotocamera è concessa, avvia l'attività della fotocamera
            takePictureLauncher.launch(null)
        } else {
            // Altrimenti, richiedi l'autorizzazione alla fotocamera
            requestCameraPermission()
        }
    }

    // Funzione per verificare se l'autorizzazione alla fotocamera è concessa
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Funzione per richiedere l'autorizzazione alla fotocamera
    private fun requestCameraPermission() {
        // Puoi utilizzare un launcher per richiedere le autorizzazioni
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Launcher per richiedere l'autorizzazione
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Se l'autorizzazione è stata concessa, avvia l'attività della fotocamera
                takePictureLauncher.launch(null)
            } else {
                // Altrimenti, gestisci il caso in cui l'utente ha negato l'autorizzazione
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

}