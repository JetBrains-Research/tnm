package dataProcessor

import dataProcessor.WorkTimeDataProcessor.UserCommitDate
import util.UserMapper
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class WorkTimeDataProcessor : DataProcessor<UserCommitDate> {
    val userMapper = UserMapper()

    // [user][minuteInWeek] = numOfCommits
    private val _workTimeDistribution = ConcurrentHashMap<Int, ConcurrentHashMap<Int, Int>>()

    val workTimeDistribution: Map<Int, Map<Int, Int>>
        get() = _workTimeDistribution

    data class UserCommitDate(val user: String, val date: Date)

    override fun processData(data: UserCommitDate) {
        val calendar: Calendar = GregorianCalendar.getInstance()
        calendar.time = data.date
        val time = (TimeUnit.DAYS.toMinutes(calendar[Calendar.DAY_OF_WEEK].toLong()) +
                TimeUnit.HOURS.toMinutes(calendar[Calendar.HOUR_OF_DAY].toLong()) +
                calendar[Calendar.MINUTE]).toInt()

        val userId = userMapper.add(data.user)

        _workTimeDistribution
            .computeIfAbsent(userId) { ConcurrentHashMap() }
            .compute(time) { _, v -> if (v == null) 1 else v + 1 }
    }

    override fun calculate() {}
}