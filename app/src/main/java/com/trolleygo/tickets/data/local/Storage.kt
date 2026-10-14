package com.trolleygo.tickets.data.local

import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket

/**
 * Локальное хранилище. Реализация по умолчанию — SharedPreferences
 * (см. SharedPrefsStorage); при необходимости заменяется на Room.
 */
interface Storage {

    /** Пресеты, которые пользователь удалил: их больше не показываем. */
    fun readRemovedPresets(): List<String>
    fun writeRemovedPresets(ids: List<String>)


    fun readRoutes(): List<Route>
    fun writeRoutes(routes: List<Route>)

    fun readTickets(): List<Ticket>
    fun writeTickets(tickets: List<Ticket>)

    /** Был ли уже выполнен первичный запуск (демонстрационные билеты). */
    var seeded: Boolean
}
