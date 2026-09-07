package com.example.prototype.feature.map.ui

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototype.data.remote.forecast.ForecastApiResult
import com.example.prototype.data.remote.forecast.ForecastRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 지도 화면을 그리는 데 필요한 모든 사용자 선택과 위치 상태다. */
data class MapUiState(
    val selectedStyleIndex: Int = 0,
    val weatherVisible: Boolean = false,
    val railwayVisible: Boolean = false,
    val locationVisible: Boolean = false,
    val currentLocation: Location? = null
)

/**
 * 지도 화면 상태와 초단기예보 수집 생명주기를 관리한다.
 * Hilt가 Repository를 주입하므로 Composable은 데이터 계층을 직접 참조하지 않는다.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val forecastRepository: ForecastRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.asStateFlow()
    private var forecastCollectionJob: Job? = null

    init {
        // 날씨가 켜지는 순간과 켜진 상태에서 새 예보가 도착할 때 Logcat에 출력한다.
        viewModelScope.launch {
            combine(
                _uiState.map { it.weatherVisible }.distinctUntilChanged(),
                forecastRepository.latestForecast
            ) { weatherVisible, forecast ->
                weatherVisible to forecast
            }.collect { (weatherVisible, forecast) ->
                if (weatherVisible) logForecastResult(forecast)
            }
        }
    }

    /** 사용자가 선택한 지도 스타일을 상태에 반영한다. */
    fun selectMapStyle(index: Int) {
        _uiState.update { it.copy(selectedStyleIndex = index) }
    }

    /** 날씨 레이어 표시 여부를 상태에 반영한다. */
    fun setWeatherVisible(visible: Boolean) {
        _uiState.update { it.copy(weatherVisible = visible) }
    }

    /** 철도 레이어 표시 여부를 상태에 반영한다. */
    fun setRailwayVisible(visible: Boolean) {
        _uiState.update { it.copy(railwayVisible = visible) }
    }

    /** 현재 위치 마커 표시 여부를 상태에 반영한다. */
    fun setLocationVisible(visible: Boolean) {
        _uiState.update { it.copy(locationVisible = visible) }
    }

    /** 기상 예보 API 최근 반환값 */
    fun getLatestForecast(): ForecastApiResult {
        return forecastRepository.latestForecast.value
    }

    /** Activity에서 조회한 Android 위치를 지도 화면 상태에 저장한다. */
    fun updateCurrentLocation(location: Location) {
        _uiState.update { it.copy(currentLocation = location) }

        // 위치가 갱신되면 이전 예약을 취소해 서로 다른 좌표의 중복 API 호출을 방지한다.
        forecastCollectionJob?.cancel()
        forecastCollectionJob = viewModelScope.launch {
            runCatching {
                forecastRepository.collectForecastEveryHour(
                    lon = location.longitude,
                    lat = location.latitude
                )
            }.onFailure { error ->
                // 좌표 변환 단계가 실패하면 원인을 남기며 다음 위치 갱신 때 다시 시작한다.
                if (error !is kotlinx.coroutines.CancellationException) {
                    Log.e(WEATHER_LOG_TAG, "격자 좌표 기반 예보 수집을 시작하지 못했습니다.", error)
                }
            }
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

        // 긴 결과가 한 줄 제한으로 잘리지 않도록 예보 항목을 각각 출력한다.
        result.forecastList.forEach { forecast ->
            Log.d(
                WEATHER_LOG_TAG,
                "${forecast.category.ctgryNm}(${forecast.category.name})=${forecast.value}(${forecast.category.ctgryUnit})"
            )
        }
    }

    private companion object {
        private const val WEATHER_LOG_TAG = "PrototypeWeather"
    }
}
