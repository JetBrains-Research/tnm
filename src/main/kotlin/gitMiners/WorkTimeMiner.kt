package gitMiners

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import util.Mapper
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.Locale

import java.util.Calendar

import org.joda.time.format.ISODateTimeFormat.hour

import java.util.GregorianCalendar

import java.util.TimeZone


/**
 * Class for mining time distribution of commits for each user.
 * As the result creates map of time and number of commits. Each
 * key (time) represents minutes in week.
 *
 * @property repository
 * @constructor Create empty Work time miner
 */
class WorkTimeMiner(
    repository: FileRepository,
    neededBranches: Set<String> = ProjectConfig.neededBranches,
    numThreads: Int = ProjectConfig.numThreads
) : GitMiner(repository, neededBranches, numThreads = numThreads) {

    // [user][minuteInWeek] = numOfCommits
    private val workTimeDistribution = ConcurrentHashMap<Int, ConcurrentHashMap<Int, AtomicInteger>>()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val email = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(email)

        val calendar: Calendar = GregorianCalendar.getInstance()
        val date = Date(currCommit.commitTime * 1000L)
        calendar.time = date

        val time = (TimeUnit.DAYS.toMinutes(calendar[Calendar.DAY_OF_WEEK].toLong()) +
                    TimeUnit.HOURS.toMinutes(calendar[Calendar.HOUR_OF_DAY].toLong()) +
                    calendar[Calendar.MINUTE]).toInt()

        workTimeDistribution
            .computeIfAbsent(userId) { ConcurrentHashMap() }
            .computeIfAbsent(time) { AtomicInteger(0) }
            .incrementAndGet()
    }

    override fun saveToJson(resourceDirectory: File) {
        UtilFunctions.saveToJson(
            File(resourceDirectory, ProjectConfig.WORKTIME_DISTRIBUTION),
            UtilFunctions.convertConcurrentMapOfConcurrentMaps(workTimeDistribution)
        )
        Mapper.saveAll(resourceDirectory)
    }
}
