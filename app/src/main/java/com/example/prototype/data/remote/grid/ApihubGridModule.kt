package com.example.prototype.data.remote.grid

import android.util.Log
import com.example.prototype.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Locale.getDefault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApihubGridModule @Inject constructor() {

    private val apiKey: String = BuildConfig.APIHUB_KMI_API_KEY

    private val gridApi: ApihubGridService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(ApihubGridService::class.java)
    }

    suspend fun getGridData(
        lon: Double,
        lat: Double
    ): GridCoord {
        Log.d(TAG, "longitude: $lon°, latitude: $lat°")
        val responseBody = gridApi.getGridData(lon, lat, HELP, apiKey)

        return withContext(Dispatchers.IO) {
            // parsing 필요
            responseBody.use { body -> parseGridResponse(body.byteStream()) }
        }
    }

    private fun parseGridResponse(inputStream: InputStream): GridCoord {
        val reader = InputStreamReader(inputStream)
        val data = reader.readLines()
        data.forEach { Log.d(TAG, it) }
        
        // data[0]: #START7777

        val cols = data[1].replace("#", "").split(",").map { it.trim() }.map {
            it.uppercase(
                getDefault()
            )
        }
        val xIdx = cols.indexOf("X")
        val yIdx = cols.indexOf("Y")

        val coords = data[2].split(",").map { it.trim() }

        return GridCoord(x = coords[xIdx].toInt(), y = coords[yIdx].toInt())
    }

    private companion object {
        private const val TAG = "ApihubGridModule"
        private const val BASE_URL = "https://apihub.kma.go.kr/"

        /** 0과 1만 사용 가능하지만 parseGridResponse에서 수월하게 파싱하기 위해 값에 대한 설명이 없는 0으로 고정 */
        private const val HELP = 0
    }
}
