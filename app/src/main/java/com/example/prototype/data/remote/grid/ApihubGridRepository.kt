package com.example.prototype.data.remote.grid

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApihubGridRepository @Inject constructor(
    private val apihubGridModule: ApihubGridModule
) {
    companion object {
        private const val TAG = "ApihubGridRepository"
    }

    suspend fun getGridData(lon: Double, lat: Double): GridCoord {
        val grid = apihubGridModule.getGridData(lon, lat)
        Log.d(TAG, "getGridData: X: ${grid.x}, Y: ${grid.y}")
        return grid
    }
}
