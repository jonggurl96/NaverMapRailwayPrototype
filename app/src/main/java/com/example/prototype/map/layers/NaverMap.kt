package com.example.prototype.map.layers

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.prototype.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapType
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberUpdatedMarkerState

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun RailwayWeatherMapScreen(
    modifier: Modifier,
    mapType: MapType = MapType.Basic,
    isDarkMode: Boolean = false,
    centerLocation: Location?
) {
    val center = if (centerLocation == null) LatLng(
        stringResource(R.string.center_lat).toDouble(),
        stringResource(R.string.center_lon).toDouble()
    ) else LatLng(centerLocation.latitude, centerLocation.longitude)

    val cameraPositionState =
        rememberCameraPositionState { position = CameraPosition(center, 11.0) }

    val properties = MapProperties(mapType = mapType, isNightModeEnabled = isDarkMode)

    NaverMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties
    ) {
        Marker(
            state = rememberUpdatedMarkerState(position = center),
            captionText = "현재 위치"
        )
    }
}

