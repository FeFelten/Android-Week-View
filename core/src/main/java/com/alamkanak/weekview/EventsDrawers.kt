package com.alamkanak.weekview

import android.graphics.Canvas
import android.text.StaticLayout
import androidx.collection.ArrayMap
import java.util.Calendar

internal class SingleEventsDrawer<T>(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache<T>
) : Drawer {

    private val eventChipDrawer = EventChipDrawer<T>(viewState)

    override fun draw(canvas: Canvas) {
        for (date in viewState.dateRange) {
            drawEventsForDate(date, canvas)
        }
    }

    private fun drawEventsForDate(
        date: Calendar,
        canvas: Canvas
    ) {
        val chips = chipsCache.normalEventChipsByDate(date)
        val valid = chips.filterNot { it.bounds.isEmpty }
        valid.forEach { eventChipDrawer.draw(it, canvas) }
    }
}

internal class AllDayEventsDrawer<T>(
    viewState: ViewState,
    private val cache: ArrayMap<EventChip<T>, StaticLayout>
) : Drawer {

    private val eventChipDrawer = EventChipDrawer<T>(viewState)

    override fun draw(canvas: Canvas) {
        for ((eventChip, textLayout) in cache) {
            eventChipDrawer.draw(eventChip, canvas, textLayout)
        }
    }
//    override fun clear() {
//        cache.clear()
//    }
}
