package com.trolleygo.tickets.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trolleygo.tickets.data.local.Storage
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.domain.Presets
import com.trolleygo.tickets.util.TicketCode
import java.util.UUID

/**
 * Билеты. Билет всегда на час; при истечении удаляется — это и есть
 * «исчезновение» билета из списка.
 */
class TicketRepository(
    private val storage: Storage,
    private val seedDemo: Boolean = true
) {

    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets

    init {
        reload()
    }

    fun reload() {
        var list = storage.readTickets()
        if (list.isEmpty() && seedDemo && !storage.seeded) {
            list = demo()
            storage.writeTickets(list)
            storage.seeded = true
        }
        _tickets.value = list.sortedByDescending { it.createdAt }
    }

    fun snapshot(): List<Ticket> = _tickets.value.orEmpty()

    fun find(id: String): Ticket? = snapshot().firstOrNull { it.id == id }

    /** Создаёт билет по маршруту и направлению; цифры вводит пользователь. */
    fun create(
        route: Route,
        direction: Direction,
        digitsInput: String,
        now: Long = System.currentTimeMillis()
    ): Ticket {
        val digits = TicketCode.digits(digitsInput)
        val ticket = Ticket(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            routeName = route.name,
            from = route.from(direction),
            to = route.to(direction),
            registration = TicketCode.registration(digits),
            boardNumber = TicketCode.boardNumber(digits),
            carrier = Presets.CARRIER,
            createdAt = now
        )
        persist(snapshot() + ticket)
        return ticket
    }

    /** Убирает истёкшие билеты: билет просто исчезает. */
    fun removeExpired(now: Long) {
        val current = snapshot()
        val alive = current.filterNot { it.isExpired(now) }
        if (alive.size != current.size) persist(alive)
    }

    fun delete(id: String) {
        persist(snapshot().filterNot { it.id == id })
    }

    /** Демонстрационные билеты первого запуска: два действующих билета. */
    private fun demo(now: Long = System.currentTimeMillis()): List<Ticket> {
        val route = Presets.all[0]
        val forward = Ticket(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            routeName = route.name,
            from = route.from(Direction.FORWARD),
            to = route.to(Direction.FORWARD),
            registration = TicketCode.registration("2021"),
            boardNumber = TicketCode.boardNumber("2021"),
            carrier = Presets.CARRIER,
            createdAt = now - 10 * 60_000L
        )
        val backward = Ticket(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            routeName = route.name,
            from = route.from(Direction.BACKWARD),
            to = route.to(Direction.BACKWARD),
            registration = TicketCode.registration("2332"),
            boardNumber = TicketCode.boardNumber("2332"),
            carrier = Presets.CARRIER,
            createdAt = now - (Ticket.DURATION_MS - 767_090L)
        )
        return listOf(forward, backward)
    }

    private fun persist(list: List<Ticket>) {
        val sorted = list.sortedByDescending { it.createdAt }
        storage.writeTickets(sorted)
        _tickets.value = sorted
    }
}
