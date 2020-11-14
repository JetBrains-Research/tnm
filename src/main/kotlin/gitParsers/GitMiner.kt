package gitParsers

import com.google.gson.Gson
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.ObjectReader
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.CanonicalTreeParser

interface GitMiner {

    val repository: Repository
    val git: Git
    val reader: ObjectReader
    val gson: Gson

    fun process(currCommit: RevCommit, prevCommit: RevCommit)

    fun run() {
        val branches: List<Ref> = git.branchList().call()

        for (branch in branches) {
            val commitsInBranch = git.log().add(repository.resolve(branch.name)).call()

            for ((currCommit, prevCommit) in commitsInBranch.windowed(2)) {
                process(currCommit, prevCommit)
            }

            // TODO: last commit and empty tree
//            val empty = repository.resolve("")
        }
    }

    fun getDiffs(currCommit: RevCommit, prevCommit: RevCommit): MutableList<DiffEntry> {
        val oldTreeIter = CanonicalTreeParser()
        oldTreeIter.reset(reader, prevCommit.tree)

        val newTreeIter = CanonicalTreeParser()
        newTreeIter.reset(reader, currCommit.tree)

        return git.diff()
                .setNewTree(newTreeIter)
                .setOldTree(oldTreeIter)
                .call()
    }

    fun saveToJson()
}