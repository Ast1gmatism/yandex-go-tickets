package com.trolleygo.tickets.ui

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window

/**
 * Экран от самого верха: прозрачный статус-бар, контент рисуется под ним
 * (градиент 78dp начинается от y=0, как в референсе), а содержимое
 * сдвигается вниз на реальную высоту статус-бара устройства.
 */
object EdgeToEdge {

    fun apply(window: Window, pad: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }
        val id = pad.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBar = if (id != 0) pad.resources.getDimensionPixelSize(id) else 0
        pad.setPadding(pad.paddingLeft, statusBar, pad.paddingRight, pad.paddingBottom)
    }
}
