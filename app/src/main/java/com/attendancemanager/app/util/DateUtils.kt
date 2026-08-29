package com.attendancemanager.app.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    fun today(): String = LocalDate.now().format(isoFormatter)

    fun toIso(date: LocalDate): String = date.format(isoFormatter)

    fun fromIso(date: String): LocalDate = LocalDate.parse(date, isoFormatter)

    fun displayDate(isoDate: String): String =
        try { LocalDate.parse(isoDate, isoFormatter).format(displayFormatter) } catch (e: Exception) { isoDate }

    fun displayMonth(yearMonth: YearMonth): String = yearMonth.format(monthFormatter)

    fun currentYearMonth(): YearMonth = YearMonth.now()

    fun monthStart(yearMonth: YearMonth): String = toIso(yearMonth.atDay(1))

    fun monthEnd(yearMonth: YearMonth): String = toIso(yearMonth.atEndOfMonth())

    fun dayOfWeekShort(isoDate: String): String =
        try {
            LocalDate.parse(isoDate, isoFormatter).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        } catch (e: Exception) { "" }
}
