package com.trolleygo.tickets.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Форматирование значений для интерфейса (русская локаль). */
object Formats {

    val RU: Locale = Locale("ru", "RU")

    private val dateTimeFormat = SimpleDateFormat("d MMMM yyyy 'г. до' HH:mm", RU)

    /** «12 октября 2026 г. до 17:15» */
    fun validUntil(expiresAt: Long): String = dateTimeFormat.format(Date(expiresAt))

    /**
     * Таймер обратного отсчёта ММ:СС:мс (последнее поле — сотые доли
     * секунды, 00…99). Максимум — 59:59:99, поэтому час показывается
     * как 59:59:99 и далее идёт вниз до 00:00:00.
     */
    fun timer(remainingMs: Long): String {
        val value = remainingMs.coerceIn(0L, 3_599_990L)
        val minutes = value / 60_000L
        val seconds = (value % 60_000L) / 1000L
        val hundredths = (value % 1000L) / 10L
        return String.format(Locale.US, "%02d:%02d:%02d", minutes, seconds, hundredths)
    }
}
