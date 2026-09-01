package com.example.prototype.data.remote.forecast

import android.util.Log
import com.example.prototype.data.remote.forecast.ForecastRepository.latestForecast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.milliseconds

/**
 * 정해진 시각마다 초단기예보를 조회하고 가장 최근 결과를 메모리에 보관한다.
 * 앱 프로세스가 유지되는 동안 최신 결과 하나를 [latestForecast]로 제공한다.
 */
object ForecastRepository {

    private const val TAG = "ForecastRepository"
    private const val FORECAST_BASE_MINUTE = 30
    private const val FETCH_MINUTE = 50

    // 기상청 발표 시각과 날짜 계산은 단말 설정과 무관하게 한국 표준시를 사용한다.
    private val koreaZone: ZoneId = ZoneId.of("Asia/Seoul")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

    private val _latestForecast = MutableStateFlow<ForecastApiResult?>(null)

    /** parse 함수로 변환되어 메모리에 보관된 가장 최근 API 결과다. */
    val latestForecast = _latestForecast.asStateFlow()

    /**
     * 매시간 50분에 API를 호출하는 반복 작업이다.
     * 이 suspend 함수가 실행 중인 코루틴이 취소되면 반복 작업과 대기도 함께 종료된다.
     */
    suspend fun collectForecastEveryHour(
        gridX: Int,
        gridY: Int,
    ) {
        while (currentCoroutineContext().isActive) {
            val now = ZonedDateTime.now(koreaZone)
            val nextFetchTime = nextFetchTime(now)

            // 다음 50분 경계까지 코루틴을 중단하므로 대기 중에는 스레드를 점유하지 않는다.
            delay(Duration.between(now, nextFetchTime).toMillis().coerceAtLeast(0L).milliseconds)
            runCatching {
                fetchAndStore(gridX = gridX, gridY = gridY)
            }.onFailure { error ->
                // 화면 종료로 취소된 작업은 오류로 처리하지 않고 즉시 상위 코루틴에 전달한다.
                if (error is CancellationException) throw error

                // 일시적인 네트워크/API 오류가 발생해도 기존 결과를 유지하고 다음 시간에 재시도한다.
                Log.e(TAG, "초단기예보 갱신에 실패했습니다.", error)
            }
        }
    }

    /** 외부 API 응답을 parse로 변환한 뒤 최신 ForecastApiResult로 교체한다. */
    private suspend fun fetchAndStore(gridX: Int, gridY: Int) {
        val requestTime = ZonedDateTime.now(koreaZone)
        val baseDate = requestTime.format(dateFormatter)
        val baseTime = requestTime
            .withHour(if (requestTime.minute >= 30) requestTime.hour else requestTime.hour - 1)
            .withMinute(FORECAST_BASE_MINUTE)
            .withSecond(0)
            .withNano(0)
            .format(timeFormatter)

        val parsedResult = ForecastModule.fetchForecast(
            baseDate = baseDate,
            baseTime = baseTime,
            gridX = gridX,
            gridY = gridY
        )

        // ForecastModule 내부에서 XML 응답을 parse한 객체만 공개 상태에 저장한다.
        _latestForecast.value = parsedResult
        Log.d(TAG, "초단기예보를 갱신했습니다: $baseDate $baseTime, 격자=($gridX, $gridY)")
    }

    /** 현재 시각보다 뒤에 있는 가장 가까운 50분 경계를 계산한다. */
    private fun nextFetchTime(now: ZonedDateTime): ZonedDateTime {
        val thisHour = now.withMinute(FETCH_MINUTE).withSecond(0).withNano(0)

        // 항상 미래 시각을 반환해 50분 동안 API가 연속 호출되는 것을 방지한다.
        return if (thisHour.isAfter(now)) thisHour else thisHour.plusHours(1)
    }
}
