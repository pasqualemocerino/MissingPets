package com.example.missingpets

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ScanActivity : AppCompatActivity() {
    private lateinit var cameraOpenId: Button
    private lateinit var clickImageId: ImageView
    private lateinit var confirmId: Button

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
        private const val pic_id = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_camera)

        cameraOpenId = findViewById(R.id.camera_button)
        clickImageId = findViewById(R.id.click_image)
        confirmId = findViewById(R.id.confirm_button)
        confirmId.isEnabled = false // lo attiviamo dopo che scatti la foto

        cameraOpenId.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }
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

        if (requestCode == pic_id && resultCode == RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as Bitmap?
            clickImageId.setImageBitmap(photo)

            // Imposta pulsante per inviare la foto al server
            confirmId.isEnabled = true
            confirmId.setOnClickListener {
                runBlocking{
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDateTime.now().format(formatter)
                    val res = PostsHandler.getBestMatchingPosts(0, date, "41.909583, 12.495029", photo!!, this@ScanActivity)
                    Log.d("RISPOSTA MATCH", res.toString())
                }
                // Apri attivita' per mostrarti il risultato del match
                startActivity(Intent(this@ScanActivity, MatchResultActivity::class.java));
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }
}