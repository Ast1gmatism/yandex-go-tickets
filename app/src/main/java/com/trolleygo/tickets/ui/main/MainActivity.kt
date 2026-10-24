package com.trolleygo.tickets.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.trolleygo.tickets.R
import com.trolleygo.tickets.ui.EdgeToEdge
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.databinding.ActivityMainBinding
import com.trolleygo.tickets.databinding.ItemTicketBinding
import com.trolleygo.tickets.ui.Fonts
import com.trolleygo.tickets.ui.detail.TicketDetailActivity

/**
 * Главный экран «Мои билеты»: заголовок с подтекстом, кнопки «назад»
 * и «обновить», список билетов. Деталей на этом экране нет — они
 * открываются вместе с билетом.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val cards = mutableListOf<TicketCardBinder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EdgeToEdge.apply(window, binding.scroll)

        binding.toolbarView.toolbarTitle.typeface = Fonts.regular(this)
        binding.toolbarView.toolbarSubtitle.typeface = Fonts.regular(this)
        binding.toolbarView.toolbarTitle.setText(R.string.screen_title)
        binding.versionChip.text = getString(
            R.string.version_chip,
            packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
        )
        binding.toolbarView.toolbarSubtitle.setText(R.string.screen_subtitle)
        binding.toolbarView.toolbarBack.apply {
            contentDescription = getString(R.string.back)
            setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }
        binding.toolbarView.toolbarAction.apply {
            contentDescription = getString(R.string.refresh)
            setOnClickListener { onRefreshClicked() }
        }
        binding.btnCreateTicket.typeface = Fonts.medium(this)
        binding.demoNotice.typeface = Fonts.regular(this)
        binding.btnCreateTicket.setOnClickListener {
            RoutePickerSheet().show(supportFragmentManager, RoutePickerSheet.TAG)
        }

        viewModel.tickets.observe(this) { renderTickets(it) }
        viewModel.now.observe(this) { now -> cards.forEach { it.render(now) } }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startTicking()
    }

    override fun onStop() {
        viewModel.stopTicking()
        super.onStop()
    }

    private fun renderTickets(tickets: List<Ticket>) {
        binding.ticketsContainer.removeAllViews()
        cards.clear()
        binding.emptyState.visibility = if (tickets.isEmpty()) View.VISIBLE else View.GONE

        tickets.forEach { ticket ->
            val itemBinding = ItemTicketBinding.inflate(
                layoutInflater, binding.ticketsContainer, false
            )
            val params = itemBinding.root.layoutParams as LinearLayout.LayoutParams
            params.bottomMargin = dp(14)
            itemBinding.root.layoutParams = params
            binding.ticketsContainer.addView(itemBinding.root)
            cards.add(
                TicketCardBinder(
                    itemBinding,
                    ticket,
                    onClick = { openTicket(it) },
                    onDelete = { onDeleteTicket(it) }
                ).also { it.renderImmediately(System.currentTimeMillis()) }
            )
        }
    }

    private fun openTicket(ticket: Ticket) {
        startActivity(
            Intent(this, TicketDetailActivity::class.java)
                .putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticket.id)
        )
    }

    private fun onDeleteTicket(ticket: Ticket) {
        viewModel.deleteTicket(ticket.id)
        Toast.makeText(this, R.string.ticket_deleted_toast, Toast.LENGTH_SHORT).show()
    }

    private fun onRefreshClicked() {
        val count = viewModel.refresh()
        Toast.makeText(this, getString(R.string.refreshed_toast, count), Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
