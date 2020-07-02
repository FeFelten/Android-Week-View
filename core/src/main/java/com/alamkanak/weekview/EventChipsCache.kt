package com.alamkanak.weekview

import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

internal class EventChipsCache<T> {

    val allEventChips: List<EventChip<T>>
        get() = normalEventChipsByDate.values.flatten() + allDayEventChipsByDate.values.flatten()

    private val normalEventChipsByDate = ConcurrentHashMap<Int, MutableList<EventChip<T>>>()
    private val allDayEventChipsByDate = ConcurrentHashMap<Int, MutableList<EventChip<T>>>()

    fun allEventChipsInDateRange(
        dateRange: List<LocalDate>
    ): List<EventChip<T>> {
        val results = mutableListOf<EventChip<T>>()
        for (date in dateRange) {
            val key = date.epochDay
            results += allDayEventChipsByDate[key].orEmpty()
            results += normalEventChipsByDate[key].orEmpty()
        }
        return results
    }

    fun normalEventChipsByDate(
        date: LocalDate
    ): List<EventChip<T>> = normalEventChipsByDate[date.epochDay].orEmpty()

    fun allDayEventChipsByDate(
        date: LocalDate
    ): List<EventChip<T>> = allDayEventChipsByDate[date.epochDay].orEmpty()

    fun allDayEventChipsInDateRange(
        dateRange: List<LocalDate>
    ): List<EventChip<T>> {
        val results = mutableListOf<EventChip<T>>()
        for (date in dateRange) {
            results += allDayEventChipsByDate[date.epochDay].orEmpty()
        }
        return results
    }

    private fun put(newChips: List<EventChip<T>>) {
        for (eventChip in newChips) {
            val key = eventChip.event.startTime.toLocalDate().epochDay
            if (eventChip.event.isAllDay) {
                allDayEventChipsByDate.addOrReplace(key, eventChip)
            } else {
                normalEventChipsByDate.addOrReplace(key, eventChip)
            }
        }
    }

    operator fun plusAssign(newChips: List<EventChip<T>>) = put(newChips)

    fun clearSingleEventsCache() {
        // TODO
        // allEventChips.filter { it.originalEvent.isNotAllDay }.forEach(EventChip<T>::clearCache)
    }

    fun clear() {
        allDayEventChipsByDate.clear()
        normalEventChipsByDate.clear()
    }

    private fun <T> ConcurrentHashMap<Int, MutableList<EventChip<T>>>.addOrReplace(
        key: Int,
        eventChip: EventChip<T>
    ) {
        val results = getOrElse(key) { mutableListOf() }
        val indexOfExisting = results.indexOfFirst { it.event.id == eventChip.event.id }
        if (indexOfExisting != -1) {
            // If an event with the same ID already exists, replace it. The new event will likely be
            // more up-to-date.
            results.removeAt(indexOfExisting)
            results.add(indexOfExisting, eventChip)
        } else {
            results.add(eventChip)
        }

        this[key] = results
    }
}
