package util

import org.eclipse.jgit.revwalk.RevCommit
import java.util.*
import kotlin.math.abs

data class TrimmedDate(val month: Int, val year: Int) : Comparable<TrimmedDate> {
  companion object {
    fun getTrimDate(date: Date): TrimmedDate {
      val calendar = Calendar.getInstance()
      calendar.time = date
      // January is 0
      val month = calendar[Calendar.MONTH] + 1
      return TrimmedDate(month, calendar[Calendar.YEAR])
    }

    fun getTrimDate(commit: RevCommit): TrimmedDate {
      val date = Date(commit.commitTime * 1000L)
      return getTrimDate(date)
    }
  }

  override fun compareTo(other: TrimmedDate): Int {
    return if (year != other.year) year.compareTo(other.year) else month.compareTo(other.month)
  }

  fun diffInMonth(other: TrimmedDate): Int {
    return abs(month - other.month) + abs(year - other.year) * 12
  }
}