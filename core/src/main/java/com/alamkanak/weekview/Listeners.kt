package com.alamkanak.weekview

import android.graphics.RectF
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar

@FunctionalInterface
interface OnEventClickListener<T> {

    /**
     * Called when an [EventChip] is clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventClick(data: T, eventRect: RectF)
}

@FunctionalInterface
interface OnEventLongClickListener<T> {

    /**
     * Called when an [EventChip] is long-clicked.
     *
     * @param data The [T] object associated with the [EventChip]'s [WeekViewEvent]
     * @param eventRect The [RectF] of the [EventChip]
     */
    fun onEventLongClick(data: T, eventRect: RectF)
}

@FunctionalInterface
interface OnEmptyViewClickListener {

    /**
     * Called when an empty area of [WeekView] is clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    // fun onEmptyViewClicked(time: Calendar) // TODO

    /**
     * Called when an empty area of [WeekView] is clicked.
     *
     * @param time The [LocalDateTime] with the date and time of the clicked position
     */
    fun onEmptyViewClicked(time: LocalDateTime)
}

@FunctionalInterface
interface OnEmptyViewLongClickListener {

    /**
     * Called when an empty area of [WeekView] is long-clicked.
     *
     * @param time A [Calendar] with the date and time of the clicked position
     */
    // fun onEmptyViewLongClick(time: Calendar) // TODO

    /**
     * Called when an empty area of [WeekView] is long-clicked.
     *
     * @param time A [LocalDateTime] with the date and time of the clicked position
     */
    fun onEmptyViewLongClick(time: LocalDateTime)
}

@FunctionalInterface
interface OnLoadMoreListener {

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     */
    // fun onLoadMore(startDate: Calendar, endDate: Calendar) // TODO

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [LocalDate] representing the start date of the month
     * @param endDate A [LocalDate] representing the end date of the month
     */
    fun onLoadMore(startDate: LocalDate, endDate: LocalDate)
}

@FunctionalInterface
interface OnRangeChangeListener {

    /**
     * Called when the range of visible days changes due to scrolling.
     *
     * @param firstVisibleDate The first visible day
     * @param lastVisibleDate The last visible day
     */
    // fun onRangeChanged(firstVisibleDate: Calendar, lastVisibleDate: Calendar) /// TODO

    /**
     * Called when the range of visible days changes due to scrolling.
     *
     * @param firstVisibleDate The first visible date
     * @param lastVisibleDate The last visible date
     */
    fun onRangeChanged(firstVisibleDate: LocalDate, lastVisibleDate: LocalDate)
}

@FunctionalInterface
interface OnMonthChangeListener<T> {

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [Calendar] representing the start date of the month
     * @param endDate A [Calendar] representing the end date of the month
     * @return The list of [WeekViewDisplayable] of the provided month
     */
    // fun onMonthChange(startDate: Calendar, endDate: Calendar): List<WeekViewDisplayable<T>> // TODO

    /**
     * Called when the month displayed in [WeekView] changes.
     * @param startDate A [LocalDate] representing the start date of the month
     * @param endDate A [LocalDate] representing the end date of the month
     * @return The list of [WeekViewDisplayable] of the provided month
     */
    fun onMonthChange(startDate: LocalDate, endDate: LocalDate): List<WeekViewDisplayable<T>>
}

@FunctionalInterface
interface ScrollListener {

    /**
     * Called when the first visible date has changed.
     *
     * @param date The new first visible date
     */
    // fun onFirstVisibleDateChanged(date: Calendar) // TODO

    /**
     * Called when the first visible date has changed.
     *
     * @param date The new first visible date
     */
    fun onFirstVisibleDateChanged(date: LocalDate)
}
