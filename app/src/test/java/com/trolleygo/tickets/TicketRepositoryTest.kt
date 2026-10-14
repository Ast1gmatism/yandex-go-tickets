package com.trolleygo.tickets

import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.data.repository.RouteRepository
import com.trolleygo.tickets.data.repository.TicketRepository
import com.trolleygo.tickets.domain.Presets
import com.trolleygo.tickets.testing.InMemoryStorage
import com.trolleygo.tickets.util.Formats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Маршруты (номер + путь) и билеты: час, номера, исчезновение. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TicketRepositoryTest {

    private lateinit var storage: InMemoryStorage

    @Before
    fun setUp() {
        storage = InMemoryStorage()
    }

    @Test
    fun `пресеты - номер троллейбуса и путь, у маршрута два направления`() {
        val routes = RouteRepository(storage)
        assertEquals(2, routes.routes.value?.size)
        val route = routes.routes.value!!.first()
        assertEquals("Троллейбус 8", route.name)
        assertEquals("площадь Свободы → Деревня Универсиады-7", route.path(Direction.FORWARD))
        assertEquals("Деревня Универсиады-7 → площадь Свободы", route.path(Direction.BACKWARD))
        // конечные точки одни и те же — меняется только направление
        assertEquals(route.endpointA, route.from(Direction.FORWARD))
        assertEquals(route.endpointA, route.to(Direction.BACKWARD))
        assertEquals("площадь Свободы ⇄ Деревня Универсиады-7", "площадь Свободы ⇄ Деревня Универсиады-7")
    }

    @Test
    fun `при первом запуске два действующих билета`() {
        val tickets = TicketRepository(storage)
        val now = System.currentTimeMillis()
        assertEquals(2, tickets.snapshot().size)
        assertTrue(tickets.snapshot().all { !it.isExpired(now) })
        assertTrue(tickets.snapshot().all { it.remainingMs(now) > 0 })
    }

    @Test
    fun `билет создаётся на час и берёт номера из введённых цифр`() {
        val tickets = TicketRepository(storage)
        val now = System.currentTimeMillis()
        val ticket = tickets.create(Presets.all[0], Direction.FORWARD, "2021", now)

        assertEquals("Троллейбус 8", ticket.routeName)
        assertEquals("площадь Свободы", ticket.from)
        assertEquals("Деревня Универсиады-7", ticket.to)
        assertEquals("ТР2021", ticket.registration)
        assertEquals("№2021", ticket.boardNumber)
        assertEquals("Троллейбусное Депо №1", ticket.carrier)
        assertEquals(now + Ticket.DURATION_MS, ticket.expiresAt)
        assertEquals("59:59:99", Formats.timer(ticket.remainingMs(now)))
        assertEquals(3600_000L - 10L, ticket.expiresAt - ticket.createdAt)
    }

    @Test
    fun `истёкший билет исчезает из списка`() {
        val tickets = TicketRepository(storage, seedDemo = false)
        val created = 1_000_000L
        val ticket = tickets.create(Presets.all[1], Direction.BACKWARD, "2332", created)
        assertNotNull(tickets.find(ticket.id))

        tickets.removeExpired(created + Ticket.DURATION_MS - 1)
        assertNotNull(tickets.find(ticket.id))

        tickets.removeExpired(created + Ticket.DURATION_MS)
        assertNull(tickets.find(ticket.id))

        assertEquals(0, tickets.snapshot().size)
    }

    @Test
    fun `маршруты удаляются без предупреждения - и свои, и пресеты`() {
        val routes = RouteRepository(storage)

        val saved = routes.add(Route("", 17, "улица Баумана", "Деревня Универсиады-7"))
        assertEquals("Троллейбус 17", saved.name)
        assertEquals(3, routes.routes.value?.size)

        val restarted = RouteRepository(storage)
        assertEquals(3, restarted.routes.value?.size)
        assertTrue(restarted.routes.value!!.any { it.id == saved.id })

        // свой маршрут
        assertTrue(restarted.remove(saved.id))
        assertEquals(2, restarted.routes.value?.size)

        // пресет тоже удаляется и не возвращается после перезапуска
        assertTrue(restarted.remove(Presets.all[0].id))
        assertEquals(1, restarted.routes.value?.size)
        val again = RouteRepository(storage)
        assertEquals(1, again.routes.value?.size)
        assertEquals(Presets.all[1].id, again.routes.value!!.first().id)
    }

    @Test
    fun `билет по новому маршруту наследует номер и остановки`() {
        val routes = RouteRepository(storage)
        val custom = routes.add(Route("", 12, "улица Баумана", "Деревня Универсиады-7"))
        val tickets = TicketRepository(storage, seedDemo = false)
        val ticket = tickets.create(custom, Direction.BACKWARD, "0777")

        assertEquals("Троллейбус 12", ticket.routeName)
        assertEquals("Деревня Универсиады-7", ticket.from)
        assertEquals("улица Баумана", ticket.to)
        assertEquals("ТР0777", ticket.registration)
    }
}
