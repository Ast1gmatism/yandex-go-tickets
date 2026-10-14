package com.trolleygo.tickets.ui.detail

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trolleygo.tickets.AppGraph
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.data.repository.TicketRepository

/** ViewModel экрана открытого билета. */
class TicketDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TicketRepository(AppGraph.storage(application), seedDemo = false)

    private val _ticket = MutableLiveData<Ticket?>()
    val ticket: LiveData<Ticket?> = _ticket

    private val _now = MutableLiveData(System.currentTimeMillis())
    val now: LiveData<Long> = _now

    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            val current = System.currentTimeMillis()
            _now.value = current
            if (_ticket.value?.isExpired(current) == true) {
                _ticket.value = null
            }
            if (ticking) handler.postDelayed(this, TICK_INTERVAL_MS)
        }
    }

    fun load(ticketId: String?) {
        repository.reload()
        _ticket.value = ticketId?.let { repository.find(it) } ?: repository.snapshot().firstOrNull()
    }

    fun startTicking() {
        if (ticking) return
        ticking = true
        handler.postDelayed(tickRunnable, TICK_INTERVAL_MS)
    }

    fun stopTicking() {
        ticking = false
        handler.removeCallbacks(tickRunnable)
    }

    override fun onCleared() {
        stopTicking()
        super.onCleared()
    }

    companion object {
        private const val TICK_INTERVAL_MS = 40L
    }
}
