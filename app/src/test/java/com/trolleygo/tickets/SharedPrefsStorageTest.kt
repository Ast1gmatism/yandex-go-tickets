package com.trolleygo.tickets

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.trolleygo.tickets.data.local.SharedPrefsStorage
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.repository.RouteRepository
import com.trolleygo.tickets.data.repository.TicketRepository
import com.trolleygo.tickets.domain.Presets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Данные лежат в SharedPreferences и переживают «перезапуск» приложения. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SharedPrefsStorageTest {

    @Test
    fun `билеты и маршруты сохраняются в SharedPreferences`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("trolley_tickets_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()

        val storage = SharedPrefsStorage(context)

        // маршруты: пресеты + пользовательский
        val routes = RouteRepository(storage)
        val custom = routes.add(Route("", 17, "улица Баумана", "Деревня Универсиады-7"))
        val afterRestart = RouteRepository(SharedPrefsStorage(context))
        assertEquals(3, afterRestart.routes.value?.size)
        assertTrue(afterRestart.routes.value!!.any { it.id == custom.id })

        // билет для первого пресета — на час, с введёнными цифрами
        val tickets = TicketRepository(storage)
        val ticket = tickets.create(Presets.all[0], Direction.BACKWARD, "2021")
        val ticketsAfterRestart = TicketRepository(SharedPrefsStorage(context), seedDemo = false)
        val restored = ticketsAfterRestart.find(ticket.id)
        assertTrue("Билет должен сохраниться", restored != null)
        assertEquals("ТР2021", restored!!.registration)
        assertEquals("№2021", restored.boardNumber)
        assertEquals("Троллейбусное Депо №1", restored.carrier)
        assertEquals(ticket.expiresAt, restored.expiresAt)
    }
}
