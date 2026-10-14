package com.trolleygo.tickets.ui.main

import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route

/** Маршрут + выбранное направление (туда или обратно). */
data class RouteChoice(val route: Route, val direction: Direction) {
    val key: String get() = route.id + ":" + direction.name
}
