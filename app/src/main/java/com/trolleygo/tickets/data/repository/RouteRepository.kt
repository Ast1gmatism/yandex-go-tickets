package com.trolleygo.tickets.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trolleygo.tickets.data.local.Storage
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.domain.Presets
import java.util.UUID

/** Маршруты: пресеты (номер + путь) и маршруты, сохранённые пользователем. */
class RouteRepository(private val storage: Storage) {

    private val _routes = MutableLiveData<List<Route>>()
    val routes: LiveData<List<Route>> = _routes

    init {
        reload()
    }

    fun reload() {
        val removed = storage.readRemovedPresets().toSet()
        _routes.value = Presets.all.filterNot { it.id in removed } +
            storage.readRoutes().filterNot { it.isPreset }
    }

    /** Сохраняет новый маршрут пользователя. */
    fun add(route: Route): Route {
        val stored = route.copy(id = UUID.randomUUID().toString(), isPreset = false)
        val custom = storage.readRoutes().filterNot { it.isPreset }.toMutableList()
        custom.removeAll { it.id == stored.id }
        custom.add(0, stored)
        storage.writeRoutes(custom)
        reload()
        return stored
    }

    /** Удаляет маршрут без предупреждения — и пресет, и пользовательский. */
    fun remove(id: String): Boolean {
        val target = _routes.value?.firstOrNull { it.id == id } ?: return false
        if (target.isPreset) {
            storage.writeRemovedPresets(storage.readRemovedPresets() + id)
        } else {
            storage.writeRoutes(
                storage.readRoutes().filterNot { it.isPreset }.filterNot { it.id == id }
            )
        }
        reload()
        return true
    }
}
