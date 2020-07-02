package com.alamkanak.weekview

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainExecutor : Executor {
    private val handler = Handler(Looper.getMainLooper())
    override fun execute(runnable: Runnable) {
        handler.post(runnable)
    }
}

/**
 * A helper class that caches the submitted [WeekViewEvent]s and creates [EventChip]s on a
 * background thread.
 */
internal class EventsDiffer<T>(
    private val context: Context,
    private val viewState: ViewState,
    private val eventsCacheWrapper: EventsCacheWrapper<T>,
    private val eventChipsLoader: EventChipsLoader<T>,
    private val eventChipsCache: EventChipsCache<T>
) {

    private val backgroundExecutor = Executors.newSingleThreadExecutor()
    private val mainThreadExecutor = MainExecutor()

    /**
     * Updates the [EventsCache] with the provided [WeekViewDisplayable]s and creates [EventChip]s.
     *
     * @param items The list of new [WeekViewDisplayable]s
     * @param onFinished Callback to inform the caller whether [WeekView] should invalidate.
     */
    fun submit(
        items: List<WeekViewDisplayable<T>>,
        onFinished: (Boolean) -> Unit
    ) {
        backgroundExecutor.execute {
            val dateRange = viewState.dateRange
            // It's possible that weekView.submit() is called before the date range has been
            // initialized. Therefore, waiting until the date range is actually set may be required.
            while (dateRange.isEmpty()) {
                Thread.sleep(100L)
                continue
            }

            val result = submitItems(items, dateRange)
            mainThreadExecutor.execute {
                onFinished(result)
            }
        }
    }

    private fun submitItems(
        items: List<WeekViewDisplayable<T>>,
        dateRange: List<LocalDate>
    ): Boolean {
        val events = items.map { it.resolve(context) }
        val startDate = events.map { it.startTime }.min()
        val endDate = events.map { it.endTime }.max()

        val eventsCache = eventsCacheWrapper.get()

        if (startDate == null || endDate == null) {
            // If these are null, this would indicate that the submitted list of events is empty.
            // The new items are empty, but it's possible that WeekView is currently displaying
            // events.
            val currentEvents = eventsCache[dateRange]
            eventsCache.clear()
            return currentEvents.isNotEmpty()
        }

        when (eventsCache) {
            is SimpleEventsCache -> eventsCache.update(events)
            is PagedEventsCache -> eventsCache.update(mapEventsToPeriod(events))
        }

        eventChipsCache += eventChipsLoader.createEventChips(events)
        return dateRange.any { it >= startDate.toLocalDate() || it <= endDate.toLocalDate() } // TODO
    }

    private fun mapEventsToPeriod(
        events: List<ResolvedWeekViewEvent<T>>
    ) = events.groupBy { YearMonth.from(it.startTime) } // Period.fromDate(it.startTime) }
}
