package com.trolleygo.tickets.data.local

import android.content.Context
import android.content.SharedPreferences
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket
import org.json.JSONArray
import org.json.JSONObject

/** Хранение маршрутов и билетов в SharedPreferences (JSON). */
class SharedPrefsStorage(context: Context) : Storage {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun readRoutes(): List<Route> = readList(KEY_ROUTES) { Route.fromJson(it) }

    override fun writeRoutes(routes: List<Route>) =
        writeList(KEY_ROUTES, routes.map { it.toJson() })

    override fun readTickets(): List<Ticket> = readList(KEY_TICKETS) { Ticket.fromJson(it) }

    override fun writeTickets(tickets: List<Ticket>) =
        writeList(KEY_TICKETS, tickets.map { it.toJson() })

    override fun readRemovedPresets(): List<String> {
        val raw = prefs.getString(KEY_REMOVED_PRESETS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { array.optString(it, null) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun writeRemovedPresets(ids: List<String>) {
        val array = JSONArray()
        ids.forEach { array.put(it) }
        prefs.edit().putString(KEY_REMOVED_PRESETS, array.toString()).commit()
    }

    override var seeded: Boolean
        get() = prefs.getBoolean(KEY_SEEDED, false)
        set(value) { prefs.edit().putBoolean(KEY_SEEDED, value).commit() }

    private fun <T> readList(key: String, parse: (JSONObject) -> T): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let { runCatching { parse(it) }.getOrNull() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun writeList(key: String, items: List<JSONObject>) {
        val array = JSONArray()
        items.forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).commit()
    }

    companion object {
        private const val PREFS_NAME = "trolley_tickets_prefs"
        private const val KEY_ROUTES = "routes"
        private const val KEY_TICKETS = "tickets"
        private const val KEY_SEEDED = "demo_seeded"
        private const val KEY_REMOVED_PRESETS = "removed_presets"
    }
}
