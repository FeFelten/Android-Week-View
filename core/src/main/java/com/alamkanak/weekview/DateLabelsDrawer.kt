package com.alamkanak.weekview

import android.graphics.Canvas
import android.text.StaticLayout
import android.util.SparseArray
import java.time.LocalDate

internal class DateLabelsDrawer(
    private val viewState: ViewState,
    private val labels: SparseArray<StaticLayout>
) : Drawer {

    override fun draw(canvas: Canvas) {
        viewState.dateRangeWithStartPixels.forEach { (date, startPixel) ->
            drawLabel(date, startPixel, canvas)
        }
    }

    private fun drawLabel(day: LocalDate, startPixel: Float, canvas: Canvas) {
        val label = labels[day.epochDay]
        canvas.withTranslation(
            x = startPixel + viewState.widthPerDay / 2,
            y = viewState.headerRowPadding.toFloat(),
            block = label::draw
        )
    }
}
