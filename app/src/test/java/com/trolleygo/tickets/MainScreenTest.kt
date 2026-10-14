package com.trolleygo.tickets

import android.content.Intent
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.repository.TicketRepository
import com.trolleygo.tickets.domain.Presets
import com.trolleygo.tickets.testing.InMemoryStorage
import com.trolleygo.tickets.ui.detail.TicketDetailActivity
import com.trolleygo.tickets.ui.main.MainActivity
import com.trolleygo.tickets.ui.widget.PassDividerView
import com.trolleygo.tickets.ui.widget.TimerDigitsView
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** Главный экран и экран открытого билета на реальном инфлейте макетов. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ru-rRU-w411dp-h891dp-xhdpi")
class MainScreenTest {

    private lateinit var storage: InMemoryStorage
    private lateinit var activity: MainActivity

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        AppGraph.storageFactory = { storage }
        ApplicationProvider.getApplicationContext<android.content.Context>()
        activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @After
    fun tearDown() {
        AppGraph.reset()
    }

    @Test
    fun `заголовок, подзаголовок и кнопки назад-обновить`() {
        assertEquals("Мои билеты", activity.findViewById<TextView>(R.id.toolbarTitle).text.toString())
        assertEquals(
            "Везде указано местное время",
            activity.findViewById<TextView>(R.id.toolbarSubtitle).text.toString()
        )
        assertNotNull(activity.findViewById<View>(R.id.toolbarBack))
        assertNotNull(activity.findViewById<View>(R.id.toolbarAction))
    }

    @Test
    fun `на главном экране только билеты - деталей нет`() {
        assertNull(activity.findViewById<View>(R.id.detailsTitle))
        assertNull(activity.findViewById<View>(R.id.rowRegistration))
        assertNotNull(activity.findViewById<View>(R.id.btnCreateTicket))
    }

    @Test
    fun `в списке два билета с зубцами, плашками таймера и подписью`() {
        val container = activity.findViewById<LinearLayout>(R.id.ticketsContainer)
        assertEquals(2, container.childCount)
        for (index in 0 until container.childCount) {
            val card = container.getChildAt(index)
            assertNotNull(card.findViewById<PassDividerView>(R.id.passDivider))
            val texts = collectTexts(card.findViewById<TimerDigitsView>(R.id.timerView))
            assertEquals(6, texts.count { it.length == 1 && it[0].isDigit() })
            assertEquals(3, texts.count { it == "мин" || it == "сек" || it == "мс" })
            assertEquals(
                "Срок действия билета",
                card.findViewById<TextView>(R.id.timerLabel).text.toString()
            )
            assertEquals(
                "Покажите экран контролеру",
                card.findViewById<TextView>(R.id.controllerNote).text.toString()
            )
        }
    }

    @Test
    fun `таймеры обоих билетов идут от часа вниз`() {
        val container = activity.findViewById<LinearLayout>(R.id.ticketsContainer)
        for (index in 0 until container.childCount) {
            val digits = collectTexts(container.getChildAt(index).findViewById<TimerDigitsView>(R.id.timerView))
                .filter { it.length == 1 && it[0].isDigit() }
                .joinToString("")
            assertEquals(6, digits.length)
            assertTrue("Таймер не должен показывать полный час: " + digits, digits != "000000")
            assertTrue(digits.substring(0, 2).toInt() in 0..59)
            assertTrue(digits.substring(2, 4).toInt() in 0..59)
            assertTrue(digits.substring(4, 6).toInt() in 0..99)
        }
    }

    @Test
    fun `нажатие на билет открывает его экран`() {
        val container = activity.findViewById<LinearLayout>(R.id.ticketsContainer)
        container.getChildAt(0).performClick()
        val started = shadowOf(activity).nextStartedActivity
        assertNotNull(started)
        assertEquals(TicketDetailActivity::class.java.name, started.component?.className)
    }

    @Test
    fun `экран билета показывает детали без лишних элементов`() {
        val ticket = TicketRepository(storage, seedDemo = false).snapshot().first()
        val intent = Intent(activity, TicketDetailActivity::class.java)
            .putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticket.id)
        val detail = Robolectric.buildActivity(TicketDetailActivity::class.java, intent).setup().get()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("Мои билеты", detail.findViewById<TextView>(R.id.toolbarTitle).text.toString())
        assertEquals("Детали", detail.findViewById<TextView>(R.id.detailsTitle).text.toString())

        val root = detail.window.decorView
        val registration = value(root, R.id.rowRegistration)
        val board = value(root, R.id.rowBoard)
        val carrier = value(root, R.id.rowCarrier)
        val validUntil = value(root, R.id.rowValidUntil)
        val direction = value(root, R.id.rowDirection)

        assertTrue("Регистрационный: " + registration, Regex("^ТР\\d{4}$").matches(registration))
        assertTrue("Бортовой: " + board, Regex("^№\\d{4}$").matches(board))
        assertEquals(registration.substring(2), board.substring(1))
        assertEquals("Троллейбусное Депо №1", carrier)
        assertTrue("Дата: " + validUntil, Regex("^\\d{1,2} [а-я]+ \\d{4} г\\. до \\d{2}:\\d{2}$").matches(validUntil))
        assertEquals("В одну сторону", direction)

        // Надписи «Билет создан» и отдельной кнопки «Назад» на экране нет — только «назад» в шапке
        val texts = collectTexts(root)
        assertTrue("Лишние надписи: " + texts, texts.none { it.startsWith("Билет создан") })
        assertTrue("Лишние надписи: " + texts, texts.none { it == "Назад" })
    }

    @Test
    fun `создание билета добавляет его в список на экране`() {
        val viewModel = androidx.lifecycle.ViewModelProvider(activity)[com.trolleygo.tickets.ui.main.MainViewModel::class.java]
        val before = viewModel.tickets.value?.size ?: 0

        viewModel.createTicket(com.trolleygo.tickets.domain.Presets.all[1], Direction.BACKWARD, "2021")
        shadowOf(Looper.getMainLooper()).idle()

        val tickets = viewModel.tickets.value ?: emptyList()
        assertEquals(before + 1, tickets.size)
        assertEquals("ТР2021", tickets.first().registration)
        assertEquals("№2021", tickets.first().boardNumber)
        assertEquals("Деревня Универсиады-7", tickets.first().from)
        assertEquals("улица Баумана", tickets.first().to)
        assertEquals(
            before + 1,
            activity.findViewById<LinearLayout>(R.id.ticketsContainer).childCount
        )
    }

    @Test
    fun `есть подпись, что это учебное приложение`() {
        val notice = activity.findViewById<TextView>(R.id.demoNotice)
        assertNotNull(notice)
        assertEquals(
            "Это учебное приложение. Билет не является проездным документом",
            notice.text.toString()
        )
    }

    @Test
    fun `билет удаляется кнопкой на карточке`() {
        val viewModel = androidx.lifecycle.ViewModelProvider(activity)[com.trolleygo.tickets.ui.main.MainViewModel::class.java]
        val before = viewModel.tickets.value?.size ?: 0
        val container = activity.findViewById<LinearLayout>(R.id.ticketsContainer)

        val delete = container.getChildAt(0).findViewById<View>(R.id.ticketDelete)
        assertNotNull(delete)
        delete.performClick()
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(before - 1, viewModel.tickets.value?.size)
        assertEquals(before - 1, container.childCount)
    }

    @Test
    fun `маршрут по пресету - два направления одного пути`() {
        val route = Presets.all[0]
        assertEquals("Троллейбус 8", route.name)
        assertEquals("площадь Свободы → Деревня Универсиады-7", route.path(Direction.FORWARD))
        assertEquals("Деревня Универсиады-7 → площадь Свободы", route.path(Direction.BACKWARD))
    }

    private fun value(root: View, rowId: Int): String =
        root.findViewById<View>(rowId).findViewById<TextView>(R.id.detailValue).text.toString()

    private fun collectTexts(view: View): List<String> {
        val result = mutableListOf<String>()
        if (view is TextView && view.text?.isNotEmpty() == true) result.add(view.text.toString())
        if (view is android.view.ViewGroup) {
            for (index in 0 until view.childCount) result.addAll(collectTexts(view.getChildAt(index)))
        }
        return result
    }
}
