package com.trolleygo.tickets.data.model

import org.json.JSONObject

/** Направление движения по маршруту. */
enum class Direction {
    FORWARD, BACKWARD;

    fun opposite(): Direction = if (this == FORWARD) BACKWARD else FORWARD
}

/**
 * Маршрут троллейбуса: хранится только номер троллейбуса и путь —
 * две конечные остановки. У маршрута два направления (туда и обратно):
 * конечные точки одни и те же, меняется только направление.
 */
data class Route(
    val id: String,
    val trolleyNumber: Int,
    val endpointA: String,
    val endpointB: String,
    val isPreset: Boolean = false
) {
    /** «Троллейбус 8» */
    val name: String get() = "Троллейбус " + trolleyNumber

    fun from(direction: Direction): String =
        if (direction == Direction.FORWARD) endpointA else endpointB

    fun to(direction: Direction): String =
        if (direction == Direction.FORWARD) endpointB else endpointA

    /** «площадь Свободы → Деревня Универсиады-7» */
    fun path(direction: Direction): String = from(direction) + " → " + to(direction)

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("trolleyNumber", trolleyNumber)
        put("endpointA", endpointA)
        put("endpointB", endpointB)
        put("isPreset", isPreset)
    }

    companion object {
        fun fromJson(json: JSONObject): Route = Route(
            id = json.getString("id"),
            trolleyNumber = json.optInt("trolleyNumber", 8),
            endpointA = json.optString("endpointA", ""),
            endpointB = json.optString("endpointB", ""),
            isPreset = json.optBoolean("isPreset", false)
        )
    }
}
