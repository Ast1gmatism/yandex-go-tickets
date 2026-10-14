package com.trolleygo.tickets.ui

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.trolleygo.tickets.R

/**
 * Шрифты приложения (YS Text). Ставим их прямо в коде: так начертание
 * (bold/medium/regular) гарантированно совпадает с макетом.
 */
object Fonts {

    fun bold(context: Context): Typeface =
        ResourcesCompat.getFont(context, R.font.ys_text_bold) ?: Typeface.DEFAULT_BOLD

    fun medium(context: Context): Typeface =
        ResourcesCompat.getFont(context, R.font.ys_text_medium) ?: Typeface.DEFAULT

    fun regular(context: Context): Typeface =
        ResourcesCompat.getFont(context, R.font.ys_text_regular) ?: Typeface.DEFAULT
}
