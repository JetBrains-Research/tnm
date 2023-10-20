package util

import dataProcessor.initData.InitData
import org.eclipse.jgit.revwalk.RevCommit
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*
import kotlin.math.abs


data class TrimmedDate(val day: Int, val month: Int, val year: Int) : Comparable<TrimmedDate>, InitData {
    companion object {
        fun getTrimDate(date: Date): TrimmedDate {
            val calendar = Calendar.getInstance()
            calendar.time = date
            // January is 0
            val month = calendar[Calendar.MONTH] + 1
            return TrimmedDate(calendar[Calendar.DAY_OF_MONTH], month, calendar[Calendar.YEAR])
        }

        fun getTrimDate(commit: RevCommit): TrimmedDate {
            val date = Date(commit.commitTime * 1000L)
            return getTrimDate(date)
        }

        fun fromLocalDate(date: LocalDate) = TrimmedDate(date.dayOfMonth, date.monthValue, date.year)

        private val weekFields = WeekFields.of(Locale.GERMANY)

    }


    override fun compareTo(other: TrimmedDate): Int {
        return if (year != other.year) year.compareTo(other.year)
        else if (month != other.month) month.compareTo(other.month)
        else day.compareTo(other.day)
    }

    fun diffInMonthIgnoreDays(other: TrimmedDate): Int {
        return abs(month - other.month) + abs(year - other.year) * 12
    }

    fun diffInWeeks(other: TrimmedDate): Int {
        var result = 0
        val (d1, d2) = listOf(this, other).sorted()
        if (d1.year != d2.year) {
            for (year in (d1.year) until d2.year) {
                result += LocalDate.of(year,12,31)[weekFields.weekOfWeekBasedYear()]
            }
            result -= d1.weekOfWeekBasedYear()
            result += d2.weekOfWeekBasedYear()
        } else {
            result = d2.weekOfWeekBasedYear() - d1.weekOfWeekBasedYear()
        }
        return result
    }

    fun toLocalDate(): LocalDate = LocalDate.of(this.year, this.month, this.day)

    fun weekOfWeekBasedYear(): Int = toLocalDate()[weekFields.weekOfWeekBasedYear()]

}
