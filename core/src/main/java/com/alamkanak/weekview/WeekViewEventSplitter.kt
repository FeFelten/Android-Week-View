package com.alamkanak.weekview

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

internal class WeekViewEventSplitter<T>(
    private val viewState: ViewState
) {

    fun split(event: ResolvedWeekViewEvent<T>): List<ResolvedWeekViewEvent<T>> {
        if (event.startTime >= event.endTime) return emptyList()
        return if (event.isMultiDay) splitEventByDates(event) else listOf(event)
    }

    private fun splitEventByDates(
        event: ResolvedWeekViewEvent<T>
    ): List<ResolvedWeekViewEvent<T>> {
        val results = mutableListOf<ResolvedWeekViewEvent<T>>()

        val firstEventEnd = event.startTime.limitToHour(viewState.maxHour)
        val firstEvent = event.copy(endTime = firstEventEnd)
        results += firstEvent

        val lastEventStart = event.endTime.withHour(viewState.minHour).truncatedTo(ChronoUnit.HOURS)
        val lastEvent = event.copy(startTime = lastEventStart)
        results += lastEvent

        val startDate = event.startTime.toLocalDate()
        val endDate = event.endTime.toLocalDate()

        val daysInBetween = startDate.datesBetween(endDate)
        for (day in daysInBetween) {
            val start = day.atStartOfDay().truncatedTo(ChronoUnit.HOURS)
            val end = start.limitToHour(viewState.maxHour)
            results += event.copy(startTime = start, endTime = end)
        }

        return results.sortedWith(compareBy({ it.startTime }, { it.endTime }))
    }
}

private fun LocalDateTime.limitToHour(hour: Int): LocalDateTime {
    return if (hour == 24) {
        this.with(ChronoField.NANO_OF_DAY, LocalTime.MAX.toNanoOfDay())
    } else {
        this.withHour(hour).truncatedTo(ChronoUnit.HOURS).minusMillis(1L)
    }
}
