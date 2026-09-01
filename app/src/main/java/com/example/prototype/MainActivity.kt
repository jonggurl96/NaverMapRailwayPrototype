package com.example.prototype

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.prototype.ui.PrototypeApp
import com.example.prototype.ui.theme.PrototypeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {

    private lateinit var fusedClient: FusedLocationProviderClient

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val preciseGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val approximateGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (preciseGranted || approximateGranted) {
            fetchCurrentLocation()
        } else {
            Toast.makeText(this, "현재 위치를 가져오려면 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun requestPermissionsIfNeeded(
        callback: (Location) -> Unit = {}
    ) {
        if (hasLocationPermission()) {
            fetchCurrentLocation(callback)
            return
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            PrototypeTheme {
                PrototypeApp(
                    getCurrentLocation = { fetchCurrentLocation(it) }
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation(
        callback: (Location) -> Unit = {}
    ) {
        if (!hasLocationPermission()) {
            requestPermissionsIfNeeded(callback)
            return
        }

        // 요청마다 새 토큰 생성
        val cancellationTokenSource = CancellationTokenSource()

        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
        ).addOnSuccessListener successListener@{ location ->
            if (location == null) {
                Log.w("LOCATION", "현재 위치를 가져오지 못했습니다.")
                return@successListener
            }

            callback(location)

            val latitude = location.latitude
            val longitude = location.longitude
            val height = location.altitude
            val accuracyMeters = location.accuracy

            Log.d(
                "Location",
                "Latitude: ${latitude}°, Longitude: ${longitude}°, Height: ${height}m, Accuracy: ${accuracyMeters}m"
            )
        }.addOnFailureListener { error ->
            Log.e("LOCATION", "위치 조회 실패", error)
        }
    }

}
