package com.trolleygo.tickets.domain

import com.trolleygo.tickets.data.model.Route

/**
 * Предустановленные маршруты: только номер троллейбуса и путь.
 * Для каждого маршрута доступны два направления — туда и обратно.
 */
object Presets {

    const val CARRIER = "Троллейбусное Депо №1"

    val all: List<Route> = listOf(
        Route(
            id = "preset_8",
            trolleyNumber = 8,
            endpointA = "площадь Свободы",
            endpointB = "Деревня Универсиады-7",
            isPreset = true
        ),
        Route(
            id = "preset_12",
            trolleyNumber = 12,
            endpointA = "улица Баумана",
            endpointB = "Деревня Универсиады-7",
            isPreset = true
        )
    )
}
