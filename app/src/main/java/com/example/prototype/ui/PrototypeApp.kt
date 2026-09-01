package com.example.prototype.ui

import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prototype.R
import com.example.prototype.data.remote.forecast.ForecastApiResult
import com.example.prototype.data.remote.forecast.ForecastRepository
import com.example.prototype.feature.map.model.MapStyleOption
import com.example.prototype.feature.map.ui.MapScreen
import com.example.prototype.feature.map.ui.component.ExpandableFabMenu
import com.example.prototype.feature.map.ui.component.MapLayerSwitch
import com.example.prototype.feature.map.ui.component.MapStyleSelector
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrototypeApp(getCurrentLocation: ((Location) -> Unit) -> Unit) {
    var selectedStyleIndex by rememberSaveable { mutableIntStateOf(0) }
    var weatherVisible by rememberSaveable { mutableStateOf(false) }
    var railwayVisible by rememberSaveable { mutableStateOf(false) }
    var locationVisible by rememberSaveable { mutableStateOf(false) }
    var currentLocation by rememberSaveable {
        mutableStateOf<Location>(Location("init").apply {
            longitude = R.string.center_lon.toDouble()
            latitude = R.string.center_lat.toDouble()
        })
    }
    val selectedStyle = MapStyleOption.entries[selectedStyleIndex]
    val latestForecast by ForecastRepository.latestForecast.collectAsStateWithLifecycle()

    // PrototypeApp이 화면에 있는 동안 매시간 50분 API 수집 작업을 실행한다.
    LaunchedEffect(Unit) {
        ForecastRepository.collectForecastEveryHour(
            currentLocation.longitude.roundToInt(),
            currentLocation.latitude.roundToInt(),
        )
    }

    // 날씨가 켜지는 순간과 켜진 상태에서 새 결과가 저장될 때마다 Logcat에 출력한다.
    LaunchedEffect(weatherVisible, latestForecast) {
        if (weatherVisible) logForecastResult(latestForecast)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Prototype") },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            )
        },
        floatingActionButton = {
            ExpandableFabMenu {
                Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
                    MapStyleSelector(selectedStyleIndex) { selectedStyleIndex = it }
                    MapLayerSwitch("날씨", weatherVisible) { weatherVisible = it }
                    MapLayerSwitch("철도", railwayVisible) { railwayVisible = it }
                    MapLayerSwitch("위치", locationVisible) { visible ->
                        locationVisible = visible
                        if (visible) getCurrentLocation { currentLocation = it }
                    }
                }
            }
        }
    ) { innerPadding ->
        MapScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            mapType = selectedStyle.mapType,
            isDarkMode = selectedStyle.isDarkMode,
            railwayVisible = railwayVisible,
            locationVisible = locationVisible,
            centerLocation = currentLocation
        )
    }
}

/** 저장된 예보를 Logcat에서 항목별로 확인할 수 있도록 출력한다. */
private fun logForecastResult(result: ForecastApiResult?) {
    if (result == null) {
        Log.d(WEATHER_LOG_TAG, "아직 저장된 초단기예보가 없습니다.")
        return
    }

    Log.d(
        WEATHER_LOG_TAG,
        "초단기예보 결과: resultCode=${result.resultCode}, " +
                "resultMsg=${result.resultMsg}, 항목 수=${result.forecastList.size}"
    )

    // 긴 결과가 Logcat 한 줄 제한으로 잘리지 않도록 예보 항목을 한 줄씩 출력한다.
    result.forecastList.forEach { forecast ->
        Log.d(
            WEATHER_LOG_TAG,
            "${forecast.category.ctgryNm}(${forecast.category.name})=${forecast.value}" +
                    forecast.category.ctgryUnit
        )
    }
}

private const val WEATHER_LOG_TAG = "PrototypeWeather"
