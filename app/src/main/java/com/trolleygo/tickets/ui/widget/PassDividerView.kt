package com.trolleygo.tickets.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import com.trolleygo.tickets.R

/**
 * Зубчатый разделитель карточки: снизу у синей шапки два боковых выреза
 * и 10 маленьких зубцов. Все вырезы расставлены с одинаковым шагом
 * (включая расстояние от бокового выреза до первого зубца), они белые и
 * только врезаются в синюю шапку — ниже её линии синего нет.
 */
class PassDividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bill_header)
        style = Paint.Style.FILL
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.bill_body)
        style = Paint.Style.FILL
    }
    /** Вырезы — цвет фона страницы (белый). */
    private val notchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }

    private var sideRadius = dp(10.5f)
    private var smallRadius = dp(4f)

    /** Толщина белой линии между шапкой и телом. */
    private val lineHeight: Float get() = dp(1.5f)
    private var smallCount = 9

    init {
        context.obtainStyledAttributes(attrs, R.styleable.PassDividerView).use { a ->
            sideRadius = a.getDimension(R.styleable.PassDividerView_sideNotchRadius, sideRadius)
            smallRadius = a.getDimension(R.styleable.PassDividerView_smallNotchRadius, smallRadius)
            smallCount = a.getInt(R.styleable.PassDividerView_smallNotchCount, smallCount)
            bluePaint.color = a.getColor(R.styleable.PassDividerView_topColor, bluePaint.color)
            bodyPaint.color = a.getColor(R.styleable.PassDividerView_bottomColor, bodyPaint.color)
        }
    }

    /** Линия низа синей шапки внутри полосы разделителя. */
    private val blueEdge: Float get() = height - dp(4f)

    /** Низ синей части шапки: 10.5dp от верха полосы (линия шапки на одном месте). */
    private val blueBottom: Float get() = dp(10.5f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f) return
        val blueBottom = this.blueBottom

        // 1) Серое тело — во всю полосу
        canvas.drawRect(0f, 0f, w, h, bodyPaint)
        // 2) Белая линия по низу шапки
        canvas.drawRect(0f, blueBottom, w, blueBottom + lineHeight, notchPaint)
        // 3) Синяя шапка — до линии
        canvas.drawRect(0f, 0f, w, blueBottom, bluePaint)
        // 4) Все вырезы — только в синей части: боковые «кругляши» и мелкие
        //    зубцы с одинаковым шагом w / (smallCount + 1)
        val save = canvas.save()
        canvas.clipRect(0f, 0f, w, blueBottom)
        canvas.drawCircle(0f, blueBottom, sideRadius, notchPaint)
        canvas.drawCircle(w, blueBottom, sideRadius, notchPaint)
        for (index in 0 until smallCount) {
            val cx = w * (index + 1) / (smallCount + 1)
            canvas.drawCircle(cx, blueBottom, smallRadius, notchPaint)
        }
        canvas.restoreToCount(save)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
