package com.example.prototype.core.json

import com.naver.maps.geometry.LatLng
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface GeoFeature {
    val properties: Map<String, String>

    data class Point(
        val position: LatLng,
        override val properties: Map<String, String>
    ) : GeoFeature

    data class LineString(
        val points: List<LatLng>,
        override val properties: Map<String, String>
    ) : GeoFeature

    data class Polygon(
        val rings: List<List<LatLng>>,
        override val properties: Map<String, String>
    ) : GeoFeature

    data class MultiPolygon(
        val polygons: List<List<List<LatLng>>>,
        override val properties: Map<String, String>
    ) : GeoFeature
}

private fun JsonArray.toLatLng(): LatLng {
    val longitude = this[0].jsonPrimitive.double
    val latitude = this[1].jsonPrimitive.double

    return LatLng(latitude, longitude)
}

private fun JsonObject.toProperties(): Map<String, String> {
    return this["properties"]?.jsonObject?.mapValues { (_, value) -> value.jsonPrimitive.content }
        .orEmpty()
}

fun parseGeoJson(geoJsonText: String): List<GeoFeature> {
    val root: JsonObject = parseToJsonElement(geoJsonText).jsonObject

    val features = root["features"]?.jsonArray.orEmpty()

    return features.mapNotNull featureLambda@{ featureElement ->
        val feature = featureElement.jsonObject
        val geometry = feature["geometry"]?.jsonObject ?: return@featureLambda null
        val type = geometry["type"]?.jsonPrimitive?.content ?: return@featureLambda null

        val coordinates = geometry["coordinates"] ?: return@featureLambda null
        val properties = feature.toProperties()

        when (type) {
            "Point" -> {
                GeoFeature.Point(
                    position = coordinates.jsonArray.toLatLng(),
                    properties = properties
                )
            }

            "LineString" -> {
                GeoFeature.LineString(
                    points = coordinates.jsonArray.map { it.jsonArray.toLatLng() },
                    properties = properties
                )
            }

            "Polygon" -> {
                GeoFeature.Polygon(
                    rings = coordinates.jsonArray.map { ring ->
                        ring.jsonArray.map { coordinate ->
                            coordinate.jsonArray.toLatLng()
                        }
                    },
                    properties = properties
                )
            }

            "MultiPolygon" -> {
                GeoFeature.MultiPolygon(
                    polygons = coordinates.jsonArray.map { polygon ->
                        polygon.jsonArray.map { ring ->
                            ring.jsonArray.map { coordinate ->
                                coordinate.jsonArray.toLatLng()
                            }
                        }
                    },
                    properties = properties
                )
            }

            else -> null
        }
    }
}