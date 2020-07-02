package com.alamkanak.weekview

import java.time.LocalDate
import java.time.YearMonth

// TODO Use Java 8 Period?
internal data class FetchRange(
    val previous: YearMonth,
    val current: YearMonth,
    val next: YearMonth
) {

    val periods: List<YearMonth> = listOf(previous, current, next)

    internal companion object {
        fun create(firstVisibleDate: LocalDate): FetchRange {
            val current = YearMonth.from(firstVisibleDate)
            return FetchRange(current.previous, current, current.next)
        }
    }
}

private val YearMonth.previous: YearMonth
    get() = minusMonths(1L)

private val YearMonth.next: YearMonth
    get() = plusMonths(1L)

// @Deprecated("")
// internal data class FetchRange(
//    val previous: Period,
//    val current: Period,
//    val next: Period
// ) {
//
//    val periods: List<Period> = listOf(previous, current, next)
//
//    fun isEqual(other: FetchRange) = this == other
//
//    internal companion object {
//        fun create(firstVisibleDay: Calendar): FetchRange {
//            val current = Period.fromDate(firstVisibleDay)
//            return FetchRange(current.previous, current, current.next)
//        }
//    }
// }
//
// // TODO Use Java 9 Period?
// internal data class Period(
//    val month: Month,
//    val year: Int
// ) : Comparable<Period> {
//
//    val previous: Period
//        get() {
//            val year = if (month == Month.JANUARY) year - 1 else year
//            val month = if (month == Month.JANUARY) Month.DECEMBER else month - 1
//            return Period(month, year)
//        }
//
//    val next: Period
//        get() {
//            val year = if (month == Month.DECEMBER) year + 1 else year
//            val month = if (month == Month.DECEMBER) Month.JANUARY else month + 1
//            return Period(month, year)
//        }
//
// //    val startDate: Calendar = newDate(year, month, dayOfMonth = 1)
// //    val endDate: Calendar = startDate.withDayOfMonth(startDate.lengthOfMonth).atEndOfDay
//
//    val startDate: LocalDate = Local newDate(year, month, dayOfMonth = 1)
//    val endDate: LocalDate = startDate.withDayOfMonth(startDate.lengthOfMonth).atEndOfDay
//
//    override fun compareTo(other: Period): Int {
//        return when {
//            year < other.year -> -1
//            year > other.year -> 1
//            else -> month.compareTo(other.month)
//        }
//    }
//
//    internal companion object {
//        // fun fromDate(date: Calendar): Period = Period(month = date.month, year = date.year)
//        fun fromDate(date: LocalDate): Period = Period(month = date.month, year = date.year)
//    }
// }
