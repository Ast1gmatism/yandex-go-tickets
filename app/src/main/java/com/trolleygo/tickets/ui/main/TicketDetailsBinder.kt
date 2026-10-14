package com.trolleygo.tickets.ui.main

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.trolleygo.tickets.R
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.ui.Fonts
import com.trolleygo.tickets.util.Formats

/** Заполняет блок «Детали» открытого билета. */
object TicketDetailsBinder {

    fun bind(root: View, ticket: Ticket?) {
        val direction = if (ticket != null) root.context.getString(R.string.direction_one_way) else ""
        bindRow(root, R.id.rowRegistration, R.drawable.ic_ticket_registration, R.string.detail_registration,
            ticket?.registration.orEmpty())
        bindRow(root, R.id.rowBoard, R.drawable.ic_ticket_bus, R.string.detail_board,
            ticket?.boardNumber.orEmpty())
        bindRow(root, R.id.rowCarrier, R.drawable.ic_ticket_carrier, R.string.detail_carrier,
            ticket?.carrier.orEmpty())
        bindRow(root, R.id.rowValidUntil, R.drawable.ic_ticket_clock, R.string.detail_valid_until,
            ticket?.let { Formats.validUntil(it.expiresAt) }.orEmpty())
        bindRow(root, R.id.rowDirection, R.drawable.ic_ticket_direction, R.string.detail_direction,
            direction, last = true)
    }

    private fun bindRow(
        root: View,
        rowId: Int,
        iconRes: Int,
        labelRes: Int,
        value: String,
        last: Boolean = false
    ) {
        val row = root.findViewById<View>(rowId) ?: return
        row.findViewById<ImageView>(R.id.detailIcon)?.setImageResource(iconRes)
        row.findViewById<TextView>(R.id.detailLabel)?.apply {
            typeface = Fonts.regular(context)
            setText(labelRes)
        }
        row.findViewById<TextView>(R.id.detailValue)?.apply {
            typeface = Fonts.regular(context)
            text = value
        }
        row.findViewById<View>(R.id.detailDivider)?.visibility = if (last) View.GONE else View.VISIBLE
    }
}
