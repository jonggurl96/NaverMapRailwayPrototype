package com.example.prototype.data.remote.forecast

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.InputStream

enum class VilageFcstCtgry(
    val ctgryNm: String,
    val ctgryUnit: String
) {
    T1H("기온", "°C"),
    RN1("1시간 강수량", "범주 (1mm)"),
    SKY("하늘상태", "코드값"),
    UUU("동서바람성분", "m/s"),
    VVV("남북바람성분", "m/s"),
    REH("습도", "%"),
    PTY("강수형태", "코드값"),
    POP("강수확률", "%"),
    LGT("낙뢰", "kA(킬로암페어)"),
    VEC("풍향", "deg"),
    WSD("풍속", "m/s");
}

fun getFcstCtgryCodeOf(code: String): VilageFcstCtgry? {
    return try {
        VilageFcstCtgry.valueOf(code.uppercase())
    } catch (iae: IllegalArgumentException) {
        null
    }

}

data class ForecastDTO(
    /** 예보 자료구분코드 카테고리 */
    var category: VilageFcstCtgry,

    /** 예보 값 */
    var value: String,
)

data class ForecastApiResult(
    val resultCode: String? = null,
    val resultMsg: String? = null,
    val forecastList: List<ForecastDTO>
)

interface UltraShortTermForecastService {

    /**
     * 기상청 초단기예보 원문을 요청한다.
     *
     * API가 XML을 반환하므로 Retrofit 변환기를 거치지 않고 [ResponseBody]로 받은 뒤,
     * 호출부에서 이 파일의 [parse] 함수로 변환한다.
     */
    @GET("getUltraSrtFcst")
    suspend fun search(
        // API_KEY는 이미 URL 인코딩된 인증키이므로 Retrofit의 중복 인코딩을 막는다.
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numberOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") gridX: Int,
        @Query("ny") gridY: Int
    ): ResponseBody

}

object ForecastModule {

    private const val BASE_URL =
        "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"

    private const val API_KEY =
        "Rj9fuQ0SJgPiFcKSEHTydhj5G81S26TCo2rriXSPt0gFcUp5WeLBWlAnyuaswUgzaQ%2BU1UBv5H1rwAPumDHBHA%3D%3D"

    private val forecastApi: UltraShortTermForecastService by lazy {
        // BASE_URL을 지정해 서비스의 상대 경로(getUltraSrtFcst)가 올바른 외부 API URL로 결합된다.
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(UltraShortTermForecastService::class.java)
    }

    /**
     * 지정한 발표 시각과 격자 좌표의 초단기예보를 조회한다.
     *
     * @param baseDate 발표 일자. `yyyyMMdd` 형식이다.
     * @param baseTime 발표 시각. 앞자리 0을 포함한 `HHmm` 형식이다.
     * @param gridX 기상청 격자 X 좌표(nx)다.
     * @param gridY 기상청 격자 Y 좌표(ny)다.
     * @return HTTP 응답 XML을 [parse]로 변환한 예보 결과다.
     */
    suspend fun fetchForecast(
        baseDate: String,
        baseTime: String,
        gridX: Int,
        gridY: Int,
        pageNo: Int = 1,
        numberOfRows: Int = 1_000
    ): ForecastApiResult {
        // 잘못된 값으로 외부 API를 호출하기 전에 입력 계약을 명확하게 확인한다.
        require(baseDate.matches(Regex("\\d{8}"))) { "baseDate는 yyyyMMdd 형식이어야 합니다." }
        require(baseTime.matches(Regex("\\d{4}"))) { "baseTime은 HHmm 형식이어야 합니다." }
        require(gridX >= 0 && gridY >= 0) { "격자 좌표는 0 이상이어야 합니다." }
        require(pageNo > 0 && numberOfRows > 0) { "페이지와 행 개수는 1 이상이어야 합니다." }

        val responseBody = forecastApi.search(
            serviceKey = API_KEY,
            pageNo = pageNo,
            numberOfRows = numberOfRows,
            dataType = "XML",
            baseDate = baseDate,
            baseTime = baseTime,
            gridX = gridX,
            gridY = gridY
        )

        // XML 스트림 파싱은 블로킹 작업이므로 IO 디스패처에서 실행하고 응답 본문은 즉시 닫는다.
        return withContext(Dispatchers.IO) {
            responseBody.use { body -> parse(body.byteStream()) }
        }
    }
}

fun parse(inputStream: InputStream): ForecastApiResult {
    val parser = Xml.newPullParser().apply {
        setInput(inputStream, "UTF-8")
    }

    val forecastList = mutableListOf<ForecastDTO>()

    var resultCode: String? = null
    var resultMessage: String? = null

    var eventType = parser.eventType

    var baseDate: String? = null
    var baseTime: Int? = null
    var fcstDate: String? = null
    var fcstTime: Int? = null

    var category: VilageFcstCtgry? = null
    var value: String? = null

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                when (parser.name) {
                    "item" -> {
                        baseDate = null
                        baseTime = null
                        fcstDate = null
                        fcstTime = null
                        category = null
                        value = null
                    }

                    "baseDate" -> {
                        baseDate = parser.nextText().trim()
                    }

                    "baseTime" -> {
                        baseTime = parser.nextText().toIntOrNull()
                    }

                    "category" -> {
                        category = getFcstCtgryCodeOf(parser.nextText().trim())
                    }

                    "fcstDate" -> {
                        fcstDate = parser.nextText().trim()
                    }

                    "fcstTime" -> {
                        fcstTime = parser.nextText().toIntOrNull()
                    }

                    "fcstValue" -> {
                        value = parser.nextText().trim()
                    }

                    "resultCode" -> {
                        resultCode = parser.nextText().trim()
                    }

                    "resultMsg" -> {
                        resultMessage = parser.nextText().trim()
                    }
                }
            }

            XmlPullParser.END_TAG -> {
                if (parser.name == "item") {
                    if (category != null && value != null)
                        forecastList += ForecastDTO(category, value)
                }
            }
        }
        eventType = parser.next()
    }

    Log.d("UltraShortTermForecast", forecastList.toString())

    return ForecastApiResult(resultCode, resultMessage, forecastList)
}
