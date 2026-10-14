package com.trolleygo.tickets.ui.detail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.trolleygo.tickets.R
import com.trolleygo.tickets.databinding.ActivityTicketDetailBinding
import com.trolleygo.tickets.ui.Fonts
import com.trolleygo.tickets.ui.main.TicketCardBinder
import com.trolleygo.tickets.ui.main.TicketDetailsBinder

/**
 * Открытый билет: заголовок «Мои билеты», карточка с идущим таймером
 * и блок «Детали». Без прокрутки и лишних элементов — как в референсе.
 * Когда срок выходит, билет исчезает (экран закрывается).
 */
class TicketDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTicketDetailBinding
    private val viewModel: TicketDetailViewModel by viewModels()
    private var card: TicketCardBinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTicketDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.detailToolbar.toolbarTitle.typeface = Fonts.bold(this)
        binding.detailToolbar.toolbarSubtitle.typeface = Fonts.regular(this)
        binding.detailsTitle.typeface = Fonts.bold(this)
        binding.detailToolbar.toolbarTitle.setText(R.string.screen_title)
        binding.detailToolbar.toolbarSubtitle.setText(R.string.screen_subtitle)
        binding.detailToolbar.toolbarBack.apply {
            contentDescription = getString(R.string.back)
            setOnClickListener { finish() }
        }
        binding.detailToolbar.toolbarAction.apply {
            contentDescription = getString(R.string.refresh)
            setOnClickListener { viewModel.load(intent.getStringExtra(EXTRA_TICKET_ID)) }
        }

        viewModel.load(intent.getStringExtra(EXTRA_TICKET_ID))

        viewModel.ticket.observe(this) { ticket ->
            if (ticket == null) {
                finish()
                return@observe
            }
            card = TicketCardBinder(binding.ticketCard, ticket)
                .also { it.renderImmediately(System.currentTimeMillis()) }
            TicketDetailsBinder.bind(binding.root, ticket)
        }

        viewModel.now.observe(this) { now -> card?.render(now) }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startTicking()
    }

    override fun onStop() {
        viewModel.stopTicking()
        super.onStop()
    }

    companion object {
        const val EXTRA_TICKET_ID = "extra_ticket_id"
    }
}
