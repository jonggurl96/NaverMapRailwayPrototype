package com.example.prototype.map.layers

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.prototype.R
import com.example.prototype.core.json.GeoFeature
import com.example.prototype.core.json.parseGeoJson
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapType
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PolygonOverlay
import com.naver.maps.map.compose.PolylineOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun RailwayWeatherMapScreen(
    modifier: Modifier,
    mapType: MapType = MapType.Basic,
    isDarkMode: Boolean = false,
    railwayVisible: Boolean = false,
    centerLocation: Location?
) {
    val context = LocalContext.current

    var features by remember {
        mutableStateOf<List<GeoFeature>>(emptyList())
    }

    LaunchedEffect(Unit) {
        features = withContext(Dispatchers.IO) {
            val geoJsonText = context.assets.open("geojson/point_output.geojson").bufferedReader()
                .use { it.readText() }

            parseGeoJson(geoJsonText)
        }
    }

    val center = if (centerLocation == null) LatLng(
        stringResource(R.string.center_lat).toDouble(),
        stringResource(R.string.center_lon).toDouble()
    ) else LatLng(centerLocation.latitude, centerLocation.longitude)

    val cameraPositionState =
        rememberCameraPositionState { position = CameraPosition(center, 11.0) }

    val properties = remember(
        mapType,
        isDarkMode
    ) {
        MapProperties(
            mapType = mapType,
            isNightModeEnabled = isDarkMode
        )
    }

    NaverMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
    ) {
        Marker(
            state = rememberUpdatedMarkerState(position = center),
            captionText = "현재 위치",
        )

        if (railwayVisible) {
            features.forEach { feature ->
                when (feature) {
                    is GeoFeature.Point -> {
                        Marker(
                            state = MarkerState(position = feature.position),
                            captionText = feature.properties["name"] ?: ""
                        )
                    }

                    is GeoFeature.LineString -> {
                        PolylineOverlay(
                            coords = feature.points,
                            color = Color(0xFF2563EB),
                            width = 6.dp
                        )
                    }

                    is GeoFeature.Polygon -> {
                        val outerRing = feature.rings.firstOrNull().orEmpty()
                        val holes = feature.rings.drop(1)

                        if (outerRing.isNotEmpty()) {
                            PolygonOverlay(
                                coords = outerRing,
                                holes = holes,
                                color = Color(0x332563EB),
                                outlineColor = Color(0xFF1D4ED8),
                                outlineWidth = 3.dp
                            )
                        }
                    }

                    is GeoFeature.MultiPolygon -> {
                        feature.polygons.forEach { polygon ->
                            val outerRing = polygon.firstOrNull().orEmpty()
                            val holes = polygon.drop(1)

                            if (outerRing.isNotEmpty()) {
                                PolygonOverlay(
                                    coords = outerRing,
                                    holes = holes,
                                    color = Color(0x332563EB),
                                    outlineColor = Color(0xFF1D4ED8),
                                    outlineWidth = 3.dp
                                )
                            }
                        }
                    }

                }
            }
        }

    }
}

