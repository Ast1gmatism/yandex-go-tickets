package com.trolleygo.tickets.ui

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Экран от самого верха: прозрачный статус-бар, окно рисует под ним
 * (градиент 78dp начинается от y=0, как в референсе), контент сдвигается
 * на реальную высоту статус-бара устройства.
 *
 * Используется WindowCompat.setDecorFitsSystemWindows(false) — современный
 * надёжный API (работает и на Android 15, и на кривых OEM-прошивках,
 * где устаревшие systemUiVisibility-флаги игнорируются и статус-бар
 * заливается системным цветом).
 */
object EdgeToEdge {

    fun apply(window: Window, pad: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val attrs = window.attributes
            attrs.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = attrs
        }

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true // тёмные иконки на светлом градиенте
        }

        val id = pad.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBar = if (id != 0) pad.resources.getDimensionPixelSize(id) else 0
        pad.setPadding(pad.paddingLeft, statusBar, pad.paddingRight, pad.paddingBottom)
    }
}
