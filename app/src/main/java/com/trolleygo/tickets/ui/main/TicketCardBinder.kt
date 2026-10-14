package com.trolleygo.tickets.ui.main

import com.trolleygo.tickets.R
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.databinding.ItemTicketBinding
import com.trolleygo.tickets.ui.Fonts
import com.trolleygo.tickets.util.Formats

/**
 * Карточка билета: шапка с маршрутом и направлением, таймер ММ:СС:мс.
 * Анимации нет — цифры просто сменяются при обновлении.
 */
class TicketCardBinder(
    private val binding: ItemTicketBinding,
    val ticket: Ticket,
    onClick: ((Ticket) -> Unit)? = null,
    onDelete: ((Ticket) -> Unit)? = null
) {

    private var lastText: String? = null

    init {
        val fonts = binding.root.context
        binding.passTitle.typeface = Fonts.bold(fonts)
        binding.passDirection.typeface = Fonts.regular(fonts)
        binding.timerLabel.typeface = Fonts.bold(fonts)
        binding.controllerNote.typeface = Fonts.regular(fonts)
        binding.passTitle.text = ticket.routeName
        binding.passDirection.text = ticket.direction
        binding.timerLabel.setText(R.string.ticket_valid)
        binding.ticketCard.contentDescription = ticket.direction
        binding.ticketDelete.visibility = if (onDelete != null) android.view.View.VISIBLE else android.view.View.GONE
        binding.ticketDelete.setOnClickListener { onDelete?.invoke(ticket) }
        if (onClick != null) {
            binding.ticketCard.setOnClickListener { onClick.invoke(ticket) }
        } else {
            binding.ticketCard.isClickable = false
            binding.ticketCard.isFocusable = false
        }
    }

    /** Обновляет значение таймера (вызывается на каждом «тике»). */
    fun render(now: Long) {
        val text = Formats.timer(ticket.remainingMs(now))
        if (text == lastText) return
        binding.timerView.render(text)
        lastText = text
    }

    fun renderImmediately(now: Long) {
        lastText = null
        render(now)
    }
}
