package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

internal class HeaderRowDrawer(
    private val viewState: ViewState
) : Drawer {

    override fun draw(canvas: Canvas) {
        val width = viewState.viewWidth.toFloat()

        val backgroundPaint = if (viewState.showHeaderRowBottomShadow) {
            viewState.headerBackgroundPaint.withShadow(
                radius = viewState.headerRowBottomShadowRadius,
                color = viewState.headerRowBottomShadowColor
            )
        } else viewState.headerBackgroundPaint

        canvas.drawRect(0f, 0f, width, viewState.headerHeight, backgroundPaint)

        if (viewState.showWeekNumber) {
            canvas.drawWeekNumber()
        }

        if (viewState.showHeaderRowBottomLine) {
            val y = viewState.headerHeight - viewState.headerRowBottomLineWidth
            canvas.drawLine(0f, y, width, y, viewState.headerRowBottomLinePaint)
        }
    }

    private fun Canvas.drawWeekNumber() {
        val field = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        val weekNumber = viewState.dateRange.first().get(field).toString()

        val bounds = viewState.weekNumberBounds
        val textPaint = viewState.weekNumberTextPaint

        val textHeight = textPaint.textHeight
        val textOffset = (textHeight / 2f).roundToInt() - textPaint.descent().roundToInt()

        val width = textPaint.getTextBounds("52").width() * 2.5f
        val height = textHeight * 1.5f

        val backgroundRect = RectF(
            bounds.centerX() - width / 2f,
            bounds.centerY() - height / 2f,
            bounds.centerX() + width / 2f,
            bounds.centerY() + height / 2f
        )

        drawRect(bounds, viewState.headerBackgroundPaint)

        val backgroundPaint = viewState.weekNumberBackgroundPaint
        val radius = viewState.weekNumberBackgroundCornerRadius.toFloat()
        drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        drawText(weekNumber, bounds.centerX(), bounds.centerY() + textOffset, textPaint)
    }
}

private val Paint.textHeight: Int
    get() = (descent() - ascent()).roundToInt()

private fun Paint.getTextBounds(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)
    return rect
}

private fun Paint.withShadow(radius: Int, color: Int): Paint {
    return Paint(this).apply {
        setShadowLayer(radius.toFloat(), 0f, 0f, color)
    }
}
