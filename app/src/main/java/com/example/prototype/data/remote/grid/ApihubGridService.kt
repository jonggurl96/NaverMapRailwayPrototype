package com.example.prototype.data.remote.grid

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface ApihubGridService {

    @GET("api/typ01/cgi-bin/url/nph-dfs_xy_lonlat")
    suspend fun getGridData(
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("help") help: Int,
        @Query("authKey") authKey: String
    ): ResponseBody
}