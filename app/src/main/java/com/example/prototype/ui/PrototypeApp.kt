package com.example.prototype.ui

import android.location.Location
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.prototype.data.remote.forecast.ForecastDTO
import com.example.prototype.feature.map.model.MapStyleOption
import com.example.prototype.feature.map.ui.MapScreen
import com.example.prototype.feature.map.ui.MapViewModel
import com.example.prototype.feature.map.ui.component.ExpandableFabMenu
import com.example.prototype.feature.map.ui.component.MapLayerSwitch
import com.example.prototype.feature.map.ui.component.MapStyleSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrototypeApp(
    getCurrentLocation: ((Location) -> Unit) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    // Lifecycle을 인식해 화면이 활성 상태일 때만 ViewModel 상태를 수집한다.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedStyle = MapStyleOption.entries[uiState.selectedStyleIndex]
    val fcstlist: List<ForecastDTO> = viewModel.getLatestForecast().forecastList

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
                    MapStyleSelector(uiState.selectedStyleIndex, viewModel::selectMapStyle)
                    MapLayerSwitch(
                        "날씨",
                        uiState.weatherVisible,
                    ) { visible ->
                        viewModel.setWeatherVisible(visible)

                        // 날씨를 처음 켤 때 경위도를 확보해 GridCoord 기반 예보 수집을 시작한다.
                        if (visible && uiState.currentLocation == null) {
                            getCurrentLocation(viewModel::updateCurrentLocation)
                        }
                    }
                    MapLayerSwitch(
                        "철도",
                        uiState.railwayVisible,
                        viewModel::setRailwayVisible
                    )
                    MapLayerSwitch("위치", uiState.locationVisible) { visible ->
                        viewModel.setLocationVisible(visible)
                        if (visible) getCurrentLocation(viewModel::updateCurrentLocation)
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
            railwayVisible = uiState.railwayVisible,
            locationVisible = uiState.locationVisible,
            weatherVisible = uiState.weatherVisible,
            centerLocation = uiState.currentLocation,
            fcstList = fcstlist,
        )

    }
}
