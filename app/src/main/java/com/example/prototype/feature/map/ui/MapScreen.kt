package com.example.prototype.feature.map.ui

import android.location.Location
import android.util.Log
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
import com.example.prototype.data.local.geojson.GeoFeature
import com.example.prototype.data.local.geojson.parseGeoJson
import com.example.prototype.data.remote.forecast.ForecastDTO
import com.example.prototype.data.remote.forecast.PtyCtgry
import com.example.prototype.data.remote.forecast.SkyCtgry
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
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
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(
    modifier: Modifier,
    mapType: MapType = MapType.Basic,
    isDarkMode: Boolean = false,
    railwayVisible: Boolean = false,
    locationVisible: Boolean = false,
    weatherVisible: Boolean = false,
    centerLocation: Location?,
    fcstList: List<ForecastDTO>,
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

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            // contentBounds를 통해 패딩을 제외한 실제 화면 영역의 LatLngBounds를 가져옵니다.
            val bounds: LatLngBounds? = cameraPositionState.contentBounds

            bounds?.let {
                val southWest = it.southWest // 남서쪽 (좌측 하단 위경도)
                val northEast = it.northEast // 북동쪽 (우측 상단 위경도)

                println("카메라 정지 - 남서쪽: ${southWest.latitude}, ${southWest.longitude}")
                println("카메라 정지 - 북동쪽: ${northEast.latitude}, ${northEast.longitude}")
            }
        }
    }

    val properties = remember(
        mapType,
        isDarkMode
    ) {
        MapProperties(
            mapType = mapType,
            isNightModeEnabled = isDarkMode
        )
    }

    val weatherText = if (fcstList.isNotEmpty()) {
        if (fcstList[0].value == PtyCtgry.NONE.ctgryDesc) fcstList[1].value else fcstList[0].value
    } else "현재날씨"

    NaverMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = properties,
    ) {
        if (weatherVisible) {
            Marker(
                state = rememberUpdatedMarkerState(position = center),
                captionText = weatherText,
                icon = OverlayImage.fromResource(getWeatherIconId(fcstList))
            )
        } else if (locationVisible) {
            Marker(
                state = rememberUpdatedMarkerState(position = center),
                captionText = "현재 위치",
                onClick = Marker@{ _ ->
                    Log.d("MapScreen", fcstList.toString())
                    return@Marker true
                }
            )
        }

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

fun getWeatherIconId(fcstList: List<ForecastDTO>): Int {
    if (fcstList.isEmpty()) return R.drawable.ic_weather_sunny
    val rainValue = fcstList[0].value
    val skyValue = fcstList[1].value

    return when (rainValue) {
        PtyCtgry.RAIN.ctgryDesc -> R.drawable.ic_weather_rain
        PtyCtgry.RNSN.ctgryDesc -> R.drawable.ic_weather_rnsn
        PtyCtgry.SNOW.ctgryDesc -> R.drawable.ic_weather_snow
        PtyCtgry.SCUD.ctgryDesc -> R.drawable.ic_weather_scud
        PtyCtgry.RAINDROP.ctgryDesc -> R.drawable.ic_weather_raindrop
        PtyCtgry.RNSNDROP.ctgryDesc -> R.drawable.ic_weather_rnsndrop
        PtyCtgry.BLOWSNOW.ctgryDesc -> R.drawable.ic_weather_blowsnow
        else -> skyWeatherIconId(skyValue)
    }
}

private fun skyWeatherIconId(value: String): Int {
    return when (value) {
        SkyCtgry.CLOUD.ctgryDesc -> R.drawable.ic_weather_cloud
        SkyCtgry.OVERCAST.ctgryDesc -> R.drawable.ic_weather_overcast
        else -> R.drawable.ic_weather_sunny
    }
}
