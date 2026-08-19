package com.example.prototype.map.fab.vo

import com.naver.maps.map.compose.MapType

class MapTypeMetadata {
    private val mapType: MapType
    private val index: Int

    private val isDarkMode: Boolean

    constructor(mapType: MapType, index: Int, isDarkMode: Boolean) {
        this.mapType = mapType
        this.index = index
        this.isDarkMode = isDarkMode
    }

    fun getMapType(): MapType {
        return mapType
    }

    fun getIndex(): Int {
        return index
    }

    fun isDarkMode(): Boolean {
        return isDarkMode
    }

}