package com.trolleygo.tickets.data.model

import org.json.JSONObject

/**
 * Билет троллейбуса. Билет создаётся ВСЕГДА на час — срок изменить нельзя.
 * Таймер стартует с 59:59:99 и идёт вниз; когда время выходит, билет
 * просто исчезает (отдельного состояния «истёк» нет).
 *
 * Регистрационный и бортовой номера используют одни и те же 4 цифры,
 * которые пользователь вводит при создании билета.
 */
data class Ticket(
    val id: String,
    val routeId: String,
    val routeName: String,
    val from: String,
    val to: String,
    val registration: String,
    val boardNumber: String,
    val carrier: String,
    val createdAt: Long
) {
    val direction: String get() = from + " → " + to

    val expiresAt: Long get() = createdAt + DURATION_MS

    fun remainingMs(now: Long): Long = (expiresAt - now).coerceAtLeast(0L)

    fun isExpired(now: Long): Boolean = now >= expiresAt

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("routeId", routeId)
        put("routeName", routeName)
        put("from", from)
        put("to", to)
        put("registration", registration)
        put("boardNumber", boardNumber)
        put("carrier", carrier)
        put("createdAt", createdAt)
    }

    companion object {
        /** Час: таймер показывает 59:59:99 -> 00:00:00. */
        const val DURATION_MS = 3_599_990L

        fun fromJson(json: JSONObject): Ticket = Ticket(
            id = json.getString("id"),
            routeId = json.optString("routeId", ""),
            routeName = json.optString("routeName", ""),
            from = json.optString("from", ""),
            to = json.optString("to", ""),
            registration = json.optString("registration", ""),
            boardNumber = json.optString("boardNumber", ""),
            carrier = json.optString("carrier", "Троллейбусное Депо №1"),
            createdAt = json.optLong("createdAt", System.currentTimeMillis())
        )
    }
}
