package com.trolleygo.tickets

import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.util.Formats
import com.trolleygo.tickets.util.TicketCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Таймер, даты и номера билета. */
class FormatsTest {

    @Test
    fun `таймер начинается с 59 59 99 - билет на час`() {
        assertEquals("59:59:99", Formats.timer(Ticket.DURATION_MS))
        assertEquals("59:59:99", Formats.timer(3_600_000L))
    }

    @Test
    fun `таймер идёт в формате ММ СС мс`() {
        assertEquals("12:47:09", Formats.timer(767_090L))
        assertEquals("00:00:01", Formats.timer(10L))
        assertEquals("47:12:99", Formats.timer(2_832_990L))
    }

    @Test
    fun `на нуле таймер показывает нули`() {
        assertEquals("00:00:00", Formats.timer(0L))
        assertEquals("00:00:00", Formats.timer(-100L))
    }

    @Test
    fun `дата действия в русском формате`() {
        val text = Formats.validUntil(1_790_000_000_000L)
        assertTrue("Получено: " + text, Regex("^\\d{1,2} [а-я]+ \\d{4} г\\. до \\d{2}:\\d{2}$").matches(text))
    }

    @Test
    fun `цифры номера превращаются в ТР и № с одинаковыми цифрами`() {
        assertEquals("2021", TicketCode.digits("20 21"))
        assertEquals("2021", TicketCode.digits("2021"))
        assertTrue(TicketCode.isValidDigits("2021"))
        assertFalse(TicketCode.isValidDigits("202"))
        assertFalse(TicketCode.isValidDigits("20а21"))
        assertEquals("ТР2021", TicketCode.registration("2021"))
        assertEquals("№2021", TicketCode.boardNumber("2021"))
        assertEquals(TicketCode.registration("2332"), "ТР2332")
        assertEquals(TicketCode.boardNumber("2332"), "№2332")
    }
}
