package com.example.prototype.feature.map.model

import com.naver.maps.map.compose.MapType

enum class MapStyleOption(
    val label: String,
    val mapType: MapType,
    val isDarkMode: Boolean
) {
    BASIC("기본지도", MapType.Basic, false),
    SATELLITE("위성지도", MapType.Satellite, false),
    DARK("다크지도", MapType.Basic, true)
}
