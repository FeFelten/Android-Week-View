package com.alamkanak.weekview

import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import androidx.collection.ArrayMap
import java.util.Calendar
import kotlin.math.roundToInt

internal interface EventsUpdater {
    val isRequired: Boolean get() = true
    fun update() = Unit
}

internal class AllDayEventsUpdater<T>(
    private val viewState: ViewState,
    private val cache: ArrayMap<EventChip<T>, StaticLayout>,
    private val chipsCache: EventChipsCache<T>
) : EventsUpdater {

    private val boundsCalculator = EventChipBoundsCalculator<T>(viewState)
    private val spannableStringBuilder = SpannableStringBuilder()

    private var previousHorizontalOrigin: Float? = null
    private var dummyTextLayout: StaticLayout? = null

    override val isRequired: Boolean
        get() {
            val didScrollHorizontally = previousHorizontalOrigin != viewState.currentOrigin.x
            val dateRange = viewState.dateRange
            val containsNewChips = chipsCache.allDayEventChipsInDateRange(dateRange).any { it.bounds.isEmpty }
            return didScrollHorizontally || containsNewChips
        }

    override fun update() {
        cache.clear()

        val datesWithStartPixels = viewState.dateRangeWithStartPixels
        for ((date, startPixel) in datesWithStartPixels) {
            // If we use a horizontal margin in the day view, we need to offset the start pixel.
            val modifiedStartPixel = when {
                viewState.isSingleDay -> startPixel + viewState.eventMarginHorizontal.toFloat()
                else -> startPixel
            }

            val eventChips = chipsCache.allDayEventChipsByDate(date)
            for (eventChip in eventChips) {
                calculateTextLayout(eventChip, modifiedStartPixel)
            }
        }

        val maximumChipHeight = cache.keys
            .mapNotNull { chip -> chip.bounds.takeIf { it.isEmpty.not() } }
            .map { it.height().roundToInt() }
            .max() ?: 0

        viewState.updateAllDayEventHeight(maximumChipHeight)
    }

    private fun calculateTextLayout(
        eventChip: EventChip<T>,
        startPixel: Float
    ) {
        val candidate = boundsCalculator.calculateAllDayEvent(eventChip, startPixel)
        if (candidate.isValid) {
            eventChip.bounds = candidate
        } else {
            eventChip.bounds.setEmpty()
        }

        if (candidate.isValid) {
            val textLayout = calculateChipTextLayout(eventChip)
            if (textLayout != null) {
                cache[eventChip] = textLayout
            }
        }
    }

    private fun calculateChipTextLayout(
        eventChip: EventChip<T>
    ): StaticLayout? {
        val event = eventChip.event
        val bounds = eventChip.bounds

        val fullHorizontalPadding = viewState.eventPaddingHorizontal * 2
        val fullVerticalPadding = viewState.eventPaddingVertical * 2

        val width = bounds.width() - fullHorizontalPadding
        val height = bounds.height() - fullVerticalPadding

        if (height < 0) {
            return null
        }

        if (width < 0) {
            // This happens if there are many all-day events
            val dummyTextLayout = createDummyTextLayout(event)
            val chipHeight = dummyTextLayout.height + fullVerticalPadding
            bounds.bottom = bounds.top + chipHeight
            return dummyTextLayout
        }

        spannableStringBuilder.clear()
        val title = event.title.emojify()
        spannableStringBuilder.append(title)

        // val title = event.title.emojify()
        // val text = SpannableStringBuilder(title)
        // text.setSpan(StyleSpan(Typeface.BOLD))

        val location = event.location?.emojify()
        if (location != null) {
            spannableStringBuilder.append(' ')
            spannableStringBuilder.append(location)
        }

        val text = spannableStringBuilder.build()
        val availableWidth = width.toInt()

        val textPaint = viewState.getTextPaint(event)
        val textLayout = text.toTextLayout(textPaint, availableWidth)
        val lineHeight = textLayout.height / textLayout.lineCount

        // For an all day event, we display just one line
        val chipHeight = lineHeight + fullVerticalPadding
        bounds.bottom = bounds.top + chipHeight

        return eventChip.ellipsizeText(text, availableWidth, existingTextLayout = textLayout)
    }

    /**
     * Creates a dummy text layout that is only used to determine the height of all-day events.
     */
    private fun createDummyTextLayout(
        event: ResolvedWeekViewEvent<T>
    ): StaticLayout {
        if (dummyTextLayout == null) {
            val textPaint = viewState.getTextPaint(event)
            dummyTextLayout = "".toTextLayout(textPaint, width = 0)
        }
        return checkNotNull(dummyTextLayout)
    }

    private fun EventChip<T>.ellipsizeText(
        text: CharSequence,
        availableWidth: Int,
        existingTextLayout: StaticLayout
    ): StaticLayout {
        val textPaint = viewState.getTextPaint(event)
        val width = bounds.width().roundToInt() - (viewState.eventPaddingHorizontal * 2)

        val ellipsized = text.ellipsized(textPaint, availableWidth)
        val isTooSmallForText = width < 0
        if (isTooSmallForText) {
            // This day contains too many all-day events. We only draw the event chips,
            // but don't attempt to draw the event titles.
            return existingTextLayout
        }

        return ellipsized.toTextLayout(textPaint, width)
    }

    private val RectF.isValid: Boolean
        get() = (left < right &&
            left < viewState.viewWidth &&
            top < viewState.viewHeight &&
            right > viewState.timeColumnWidth &&
            bottom > 0)
}

internal class SingleEventsUpdater<T>(
    private val viewState: ViewState,
    private val chipsCache: EventChipsCache<T>
) : EventsUpdater {

    private val boundsCalculator = EventChipBoundsCalculator<T>(viewState)

    override fun update() {
        // chipsCache.clearSingleEventsCache()

        viewState
            .dateRangeWithStartPixels
            .forEach { (date, startPixel) ->
                // If we use a horizontal margin in the day view, we need to offset the start pixel.
                val modifiedStartPixel = when {
                    viewState.isSingleDay -> startPixel + viewState.eventMarginHorizontal.toFloat()
                    else -> startPixel
                }
                calculateRectsForEventsOnDate(date, modifiedStartPixel)
            }
    }

    private fun calculateRectsForEventsOnDate(
        date: Calendar,
        startPixel: Float
    ) {
        chipsCache.normalEventChipsByDate(date)
            .filter { it.event.isNotAllDay && it.event.isWithin(viewState.minHour, viewState.maxHour) }
            .forEach {
                val candidate = boundsCalculator.calculateSingleEvent(it, startPixel)
                if (candidate.isValid) {
                    it.bounds = candidate
                } else {
                    it.bounds.setEmpty()
                }
            }
    }

    private val RectF.isValid: Boolean
        get() {
            val hasCorrectWidth = left < right && left < viewState.viewWidth
            val hasCorrectHeight = top < viewState.viewHeight
            val isNotHiddenByChrome = right > viewState.timeColumnWidth && bottom > viewState.headerHeight
            return hasCorrectWidth && hasCorrectHeight && isNotHiddenByChrome
        }
}
