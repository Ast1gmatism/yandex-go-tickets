package com.trolleygo.tickets.ui.main

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.trolleygo.tickets.AppGraph
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.data.model.Route
import com.trolleygo.tickets.data.model.Ticket
import com.trolleygo.tickets.data.repository.RouteRepository
import com.trolleygo.tickets.data.repository.TicketRepository

/**
 * ViewModel главного экрана: список билетов, маршруты и «тик» времени.
 * Истёкшие билеты сами удаляются — билет просто исчезает из списка.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = AppGraph.storage(application)
    private val routeRepository = RouteRepository(storage)
    private val ticketRepository = TicketRepository(storage)

    val routes: LiveData<List<Route>> = routeRepository.routes
    val tickets: LiveData<List<Ticket>> = ticketRepository.tickets

    private val _now = MutableLiveData(System.currentTimeMillis())
    val now: LiveData<Long> = _now

    private val handler = Handler(Looper.getMainLooper())
    private var ticking = false

    private val tickRunnable = object : Runnable {
        override fun run() {
            val current = System.currentTimeMillis()
            _now.value = current
            ticketRepository.removeExpired(current)
            if (ticking) handler.postDelayed(this, TICK_INTERVAL_MS)
        }
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

    /** Кнопка «обновить»: перечитать данные и снять истёкшие билеты. */
    fun refresh(): Int {
        routeRepository.reload()
        ticketRepository.reload()
        ticketRepository.removeExpired(System.currentTimeMillis())
        _now.value = System.currentTimeMillis()
        return tickets.value?.size ?: 0
    }

    fun createTicket(route: Route, direction: Direction, digits: String): Ticket =
        ticketRepository.create(route, direction, digits)

    fun addRoute(trolleyNumber: Int, stopA: String, stopB: String): Route =
        routeRepository.add(Route("", trolleyNumber, stopA, stopB))

    fun deleteTicket(id: String) = ticketRepository.delete(id)

    fun deleteRoute(id: String): Boolean = routeRepository.remove(id)

    companion object {
        /** ~25 кадров в секунду: цифры мс успевают меняться без анимации. */
        const val TICK_INTERVAL_MS = 40L
    }
}
