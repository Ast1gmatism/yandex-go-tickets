package com.trolleygo.tickets.util

import java.util.Locale
import kotlin.random.Random

/**
 * Номера билета. Пользователь вводит 4 цифры номера троллейбуса —
 * из них получаются оба номера: регистрационный «ТР» + цифры и
 * бортовой «№» + те же цифры.
 */
object TicketCode {

    /** Длина номера, который вводит пользователь. */
    const val DIGIT_LENGTH = 4

    fun digits(input: String?): String = (input ?: "").filter { it in '0'..'9' }.take(DIGIT_LENGTH)

    fun isValidDigits(input: String?): Boolean =
        input != null && input.length == DIGIT_LENGTH && input.all { it in '0'..'9' }

    fun registration(input: String?): String = "ТР" + digits(input).padStart(4, '0')

    fun boardNumber(input: String?): String = "№" + digits(input).padStart(4, '0')

    fun randomDigits(): String = String.format(Locale.US, "%04d", Random.nextInt(1, 9999))
}
