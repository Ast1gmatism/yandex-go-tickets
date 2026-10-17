package com.trolleygo.tickets

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Looper
import android.view.View
import com.trolleygo.tickets.data.model.Direction
import com.trolleygo.tickets.testing.InMemoryStorage
import com.trolleygo.tickets.ui.detail.TicketDetailActivity
import com.trolleygo.tickets.ui.main.MainActivity
import com.trolleygo.tickets.ui.main.RouteChoice
import com.trolleygo.tickets.ui.main.RouteRowBinder
import com.trolleygo.tickets.domain.Presets
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/** Рендер экранов в PNG (Robolectric NATIVE). Каталог — через TROLLEY_SHOT_DIR, по умолчанию /home/user. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "ru-rRU-w411dp-h891dp-xhdpi")
class ScreenshotTest {

    private val density = 2.0f
    private val screenWidth = (411 * density).toInt()
    private val screenHeight = (891 * density).toInt()
    private lateinit var storage: InMemoryStorage

    @Before
    fun setUp() {
        storage = InMemoryStorage()
        AppGraph.storageFactory = { storage }
        // Первый запуск: два демонстрационных билета на час
        com.trolleygo.tickets.data.repository.TicketRepository(storage)
    }

    @After
    fun tearDown() {
        AppGraph.reset()
    }

    @Test
    fun `рендер главного экрана и открытого билета`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(Looper.getMainLooper()).idle()
        drawToFile(activity.window.decorView, screenWidth, screenHeight, "screenshots/main_viewport.png")

        val container = activity.findViewById<android.view.ViewGroup>(R.id.ticketsContainer)
        assertTrue("Ожидались два билета", container.childCount >= 2)
        drawToFile(container.getChildAt(0), screenWidth - (32 * density).toInt() * 2, 0,
            "screenshots/ticket_card.png")

        val ticket = com.trolleygo.tickets.data.repository.TicketRepository(storage, seedDemo = false)
            .snapshot().first()
        val intent = Intent(activity, TicketDetailActivity::class.java)
            .putExtra(TicketDetailActivity.EXTRA_TICKET_ID, ticket.id)
        val detail = Robolectric.buildActivity(TicketDetailActivity::class.java, intent).setup().get()
        shadowOf(Looper.getMainLooper()).idle()
        drawToFile(detail.window.decorView, screenWidth, screenHeight, "screenshots/detail_viewport.png")
    }

    @Test
    fun `рендер нижних листов`() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        shadowOf(Looper.getMainLooper()).idle()

        val picker = com.trolleygo.tickets.databinding.DialogRoutePickerBinding
            .inflate(activity.layoutInflater)
        Presets.all.forEach { route ->
            Direction.values().forEach { direction ->
                val row = com.trolleygo.tickets.databinding.ItemRouteBinding.inflate(
                    activity.layoutInflater, picker.routeListContainer, false
                )
                RouteRowBinder.bind(
                    row,
                    RouteChoice(route, direction),
                    selected = route == Presets.all[0] && direction == Direction.FORWARD,
                    onClick = {},
                    onDelete = {}
                )
                picker.routeListContainer.addView(row.root)
            }
        }
        com.trolleygo.tickets.ui.main.SheetFonts.apply(activity, picker)
        picker.editDigits.setText("2021")
        shadowOf(Looper.getMainLooper()).idle()
        drawToFile(picker.root, screenWidth, 0, "screenshots/sheet_new_ticket.png")

        val addRoute = com.trolleygo.tickets.databinding.SheetAddRouteBinding
            .inflate(activity.layoutInflater)
        com.trolleygo.tickets.ui.main.SheetFonts.apply(activity, addRoute)
        addRoute.editTrolley.setText("8")
        addRoute.editStopA.setText("площадь Свободы")
        addRoute.editStopB.setText("Деревня Универсиады-7")
        shadowOf(Looper.getMainLooper()).idle()
        drawToFile(addRoute.root, screenWidth, 0, "screenshots/sheet_new_route.png")
    }

    private fun drawToFile(view: View, width: Int, height: Int, path: String) {
        val target = if (height > 0) height else {
            view.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.measuredHeight
        }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(target, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, target)
        shadowOf(Looper.getMainLooper()).idle()

        val bitmap = Bitmap.createBitmap(width, target, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        // Каталог можно переопределить (CI), по умолчанию — как в песочнице
        val base = System.getenv("TROLLEY_SHOT_DIR") ?: "/home/user"
        val file = File(base, path)
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("Снимок: " + file.absolutePath + " " + width + "x" + target)
    }
}
