package gitMiners

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.revwalk.RevCommit
import util.ProjectConfig
import util.UserMapper
import util.UtilFunctions
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * Class for mining time distribution of commits for each user.
 * As the result creates map of time and number of commits. Each
 * key (time) represents minutes in week.
 *
 * @property repository
 * @constructor Create empty Work time miner
 */
class WorkTimeMiner(override val repository: FileRepository) : GitMiner() {
    override val git = Git(repository)
    override val reader: ObjectReader = repository.newObjectReader()

    // [user][minuteInWeek] = numOfCommits
    private val workTimeDistribution = HashMap<Int, HashMap<Int, Int>>()
    private val calendar: Calendar = GregorianCalendar.getInstance()

    override fun process(currCommit: RevCommit, prevCommit: RevCommit) {
        val email = currCommit.authorIdent.emailAddress
        val userId = UserMapper.add(email)

        val date = Date(currCommit.commitTime * 1000L)
        calendar.time = date

        val time =
            (TimeUnit.HOURS.toMinutes(calendar[Calendar.HOUR_OF_DAY].toLong()) + calendar[Calendar.MINUTE]).toInt()
        val newValue = workTimeDistribution
            .computeIfAbsent(userId) { HashMap() }
            .computeIfAbsent(time) { 0 }
            .inc()

        workTimeDistribution
            .computeIfAbsent(userId) { HashMap() }[time] = newValue

    }

    override fun saveToJson() {
        UtilFunctions.saveToJson(ProjectConfig.WORKTIME_DISTRIBUTION_PATH, workTimeDistribution)
    }
}

fun main() {
    val miner = WorkTimeMiner(ProjectConfig.repository)
    miner.run()
    miner.saveToJson()
}