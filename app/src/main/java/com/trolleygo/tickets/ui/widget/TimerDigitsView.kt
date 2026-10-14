package com.trolleygo.tickets.ui.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.trolleygo.tickets.R

/**
 * Таймер ММ:СС:мс. Каждая цифра — белая плашка на сером теле билета.
 * Двоеточия выровнены по низу плашек (чуть выше нижней кромки),
 * подписи «мин / сек / мс» — чёрные. Анимации нет: цифры просто
 * сменяются при обновлении значения.
 */
class TimerDigitsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val chips = ArrayList<TextView>()
    private val colons = ArrayList<TextView>()
    private val units = ArrayList<TextView>()

    private val chipsRow = LinearLayout(context)
    private val unitsRow = LinearLayout(context)

    private val boldTypeface: Typeface =
        ResourcesCompat.getFont(context, R.font.ys_text_bold) ?: Typeface.DEFAULT_BOLD
    private val regularTypeface: Typeface =
        ResourcesCompat.getFont(context, R.font.ys_text_regular) ?: Typeface.DEFAULT

    private val chipWidth = dp(38)
    private val chipHeight = dp(52)
    private val pairGap = dp(4)
    private val colonSlot = dp(22)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL

        chipsRow.orientation = HORIZONTAL
        chipsRow.gravity = Gravity.CENTER
        unitsRow.orientation = HORIZONTAL
        unitsRow.gravity = Gravity.CENTER

        addView(chipsRow, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
        addView(
            unitsRow,
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
            }
        )

        repeat(6) {
            chips.add(
                TextView(context).apply {
                    gravity = Gravity.CENTER
                    typeface = boldTypeface
                    includeFontPadding = false
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 28f)
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    setBackgroundResource(R.drawable.bg_timer_chip)
                    text = "0"
                }
            )
        }
        repeat(2) {
            colons.add(
                TextView(context).apply {
                    // Двоеточие выровнено по низу плашки, чуть выше её кромки
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    typeface = boldTypeface
                    includeFontPadding = false
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    text = ":"
                    setPadding(0, 0, 0, dp(7))
                }
            )
        }
        listOf(R.string.unit_min, R.string.unit_sec, R.string.unit_ms).forEach { res ->
            units.add(
                TextView(context).apply {
                    gravity = Gravity.CENTER
                    typeface = regularTypeface
                    includeFontPadding = false
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(ContextCompat.getColor(context, R.color.text_units))
                    setText(res)
                }
            )
        }

        chipsRow.addView(chips[0], params(chipWidth, chipHeight))
        chipsRow.addView(chips[1], params(chipWidth, chipHeight, marginStart = pairGap))
        chipsRow.addView(colons[0], params(colonSlot, chipHeight, marginStart = 0, marginEnd = 0))
        chipsRow.addView(chips[2], params(chipWidth, chipHeight))
        chipsRow.addView(chips[3], params(chipWidth, chipHeight, marginStart = pairGap))
        chipsRow.addView(colons[1], params(colonSlot, chipHeight, marginStart = 0, marginEnd = 0))
        chipsRow.addView(chips[4], params(chipWidth, chipHeight))
        chipsRow.addView(chips[5], params(chipWidth, chipHeight, marginStart = pairGap))

        val groupWidth = chipWidth * 2 + pairGap
        val colonWidth = colonSlot
        unitsRow.addView(units[0], params(groupWidth, LayoutParams.WRAP_CONTENT))
        unitsRow.addView(spacer(), params(colonWidth, dp(1)))
        unitsRow.addView(units[1], params(groupWidth, LayoutParams.WRAP_CONTENT))
        unitsRow.addView(spacer(), params(colonWidth, dp(1)))
        unitsRow.addView(units[2], params(groupWidth, LayoutParams.WRAP_CONTENT))

        render("59:59:99")
    }

    /** Обновляет значение таймера (без анимации — цифры просто сменяются). */
    fun render(text: String) {
        val value = if (text.length >= 8) text else "00:00:00"
        chips[0].text = value[0].toString()
        chips[1].text = value[1].toString()
        chips[2].text = value[3].toString()
        chips[3].text = value[4].toString()
        chips[4].text = value[6].toString()
        chips[5].text = value[7].toString()
    }

    private fun params(
        width: Int,
        height: Int,
        marginStart: Int = 0,
        marginEnd: Int = 0
    ): LayoutParams = LayoutParams(width, height).apply {
        this.marginStart = marginStart
        this.marginEnd = marginEnd
    }

    private fun spacer(): View = View(context)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
