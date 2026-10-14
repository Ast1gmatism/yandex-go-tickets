package com.trolleygo.tickets.testing

import com.trolleygo.tickets.data.local.Storage
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket

/** Хранилище в памяти: тесты UI не зависят от файловой системы. */
class InMemoryStorage : Storage {

    private var routes: List<Route> = emptyList()
    private var tickets: List<Ticket> = emptyList()

    override fun readRoutes(): List<Route> = routes
    override fun writeRoutes(routes: List<Route>) { this.routes = routes }

    override fun readTickets(): List<Ticket> = tickets
    override fun writeTickets(tickets: List<Ticket>) { this.tickets = tickets }

    private var removedPresets: List<String> = emptyList()

    override fun readRemovedPresets(): List<String> = removedPresets
    override fun writeRemovedPresets(ids: List<String>) { removedPresets = ids }

    override var seeded: Boolean = false
}
