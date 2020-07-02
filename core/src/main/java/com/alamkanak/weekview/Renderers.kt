package com.alamkanak.weekview

import android.graphics.Canvas
import android.graphics.RectF
import android.text.StaticLayout
import android.util.SparseArray
import androidx.collection.ArrayMap
import java.time.LocalDate

internal interface Renderer {
    // fun updateBounds(bounds: RectF, canvas: Canvas)
    fun render(canvas: Canvas)
    fun onInvalidate() = Unit
}

internal interface Drawer {
    fun draw(canvas: Canvas)
}

internal class CalendarRenderer<T>(
    private val viewState: ViewState,
    eventChipCache: EventChipsCache<T>
) : Renderer {

    private val updater = SingleEventsUpdater(viewState, eventChipCache)
    private val bounds = RectF()

    private val drawers = listOf(
        DayBackgroundDrawer(viewState),
        BackgroundGridDrawer(viewState),
        SingleEventsDrawer(viewState, eventChipCache),
        NowLineDrawer(viewState)
    )

    override fun render(canvas: Canvas) {
        if (updater.isRequired) {
            updater.update()
        }

        updateBounds(canvas)
        canvas.inBounds(bounds, this::renderContent)
    }

    private fun renderContent(canvas: Canvas) {
        for (drawer in drawers) {
            drawer.draw(canvas)
        }
    }

    private fun updateBounds(canvas: Canvas) {
        bounds.left = viewState.timeColumnWidth
        bounds.top = viewState.headerHeight
        bounds.right = canvas.width.toFloat()
        bounds.bottom = canvas.height.toFloat()
    }
}

internal class TimeColumnRenderer(
    private val viewState: ViewState
) : Renderer {

    private val labels = SparseArray<StaticLayout>()
    private val bounds = RectF()

    private val displayedHours: IntProgression
        get() = viewState.timeRange step viewState.timeColumnHoursInterval

    init {
        cacheTimeLabels()
    }

    override fun render(canvas: Canvas) = with(viewState) {
        updateBounds(canvas)
        canvas.inBounds(bounds) {
            renderContent()
        }
    }

    private fun updateBounds(canvas: Canvas) {
        bounds.top = viewState.headerHeight
        bounds.right = viewState.timeColumnWidth
        bounds.bottom = canvas.height.toFloat()
    }

    private fun Canvas.renderContent() = with(viewState) {
        var topMargin = headerHeight
        val bottom = viewHeight.toFloat()

        drawRect(0f, topMargin, timeColumnWidth, bottom, timeColumnBackgroundPaint)

        val hourLines = FloatArray(hoursPerDay * 4)

        for (hour in displayedHours) {
            val heightOfHour = hourHeight * (hour - minHour)
            topMargin = headerHeight + currentOrigin.y + heightOfHour

            val isOutsideVisibleArea = topMargin > bottom
            if (isOutsideVisibleArea) {
                continue
            }

            val x = timeTextWidth + timeColumnPadding
            var y = topMargin - timeTextHeight / 2

            // If the hour separator is shown in the time column, move the time label below it
            if (showTimeColumnHourSeparator) {
                y += timeTextHeight / 2 + hourSeparatorPaint.strokeWidth + timeColumnPadding
            }

            val textLayout = labels[hour]
            withTranslation(x, y) {
                textLayout.draw(this)
            }

            if (showTimeColumnHourSeparator && hour > 0) {
                val j = hour - 1
                hourLines[j * 4] = 0f
                hourLines[j * 4 + 1] = topMargin
                hourLines[j * 4 + 2] = timeColumnWidth
                hourLines[j * 4 + 3] = topMargin
            }
        }

        // Draw the vertical time column separator
        if (showTimeColumnSeparator) {
            val lineX = timeColumnWidth - timeColumnSeparatorStrokeWidth
            drawLine(lineX, headerHeight, lineX, bottom, timeColumnSeparatorPaint)
        }

        // Draw the hour separator inside the time column
        if (showTimeColumnHourSeparator) {
            drawLines(hourLines, hourSeparatorPaint)
        }
    }

    private fun cacheTimeLabels() = with(viewState) {
        for (hour in displayedHours) {
            val textLayout = timeFormatter(hour).toTextLayout(timeTextPaint)
            labels.put(hour, textLayout)
        }
    }

    override fun onInvalidate() {
        labels.clear()
        cacheTimeLabels()
    }
}

internal class HeaderRenderer<T>(
    private val viewState: ViewState,
    eventChipCache: EventChipsCache<T>
) : Renderer {

    private val eventLayouts = ArrayMap<EventChip<T>, StaticLayout>()
    private val eventsUpdater = AllDayEventsUpdater(viewState, eventLayouts, eventChipCache)
    private val eventsDrawer = AllDayEventsDrawer(viewState, eventLayouts)

    private val dateLabelLayouts = SparseArray<StaticLayout>()
    private val dateLabelBounds = RectF()

    private val backgroundDrawer = HeaderRowDrawer(viewState)
    private val dateLabelsDrawer = DateLabelsDrawer(viewState, dateLabelLayouts)

    override fun render(canvas: Canvas) {
        updateHeader()
        backgroundDrawer.draw(canvas)

        // Restrict the bounds for the date labels and event chips,
        // as to not interfere with the week number
        dateLabelBounds.set(canvas.clipBounds)
        dateLabelBounds.left = viewState.timeColumnWidth

        canvas.inBounds(dateLabelBounds) {
            eventsDrawer.draw(canvas)
            dateLabelsDrawer.draw(this)
            eventsDrawer.draw(this)
        }
    }

    private fun updateHeader() {
        // FIXME: Only update if something changed
        val maxDateLabelHeight = updateDateLabels(dateRange = viewState.dateRange)
        viewState.headerTextHeight = maxDateLabelHeight.toFloat()

        if (eventsUpdater.isRequired) {
            eventsUpdater.update()
        }
    }

    private fun updateDateLabels(dateRange: List<LocalDate>): Int {
        val keys = dateRange.map { it.epochDay }
        val textLayouts = dateRange.map { viewState.calculateStaticLayoutForDate(it) }

        dateLabelLayouts.clear()
        dateLabelLayouts += keys.zip(textLayouts).toMap()

        return textLayouts.map { it.height }.max() ?: 0
    }

    private fun ViewState.calculateStaticLayoutForDate(date: LocalDate): StaticLayout {
        val dayLabel = dateFormatter(date)
        return dayLabel.toTextLayout(
            textPaint = if (date.isToday) todayHeaderTextPaint else headerTextPaint,
            width = totalDayWidth.toInt()
        )
    }

    private operator fun <E> SparseArray<E>.plusAssign(elements: Map<Int, E>) {
        elements.entries.forEach { put(it.key, it.value) }
    }
}

private fun Canvas.inBounds(
    bounds: RectF,
    block: Canvas.() -> Unit
) {
    save()
    clipRect(bounds)
    block()
    restore()
}
