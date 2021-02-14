package util

import java.util.*

class StorageN<T>(private val numberOfElements: Int, private val comparator: Comparator<T>) {
    private val priorityQueue = PriorityQueue(comparator)

    var low: T? = null
        private set

    var high: T? = null
        private set

    fun add(value: T) {
        if (priorityQueue.isNotEmpty()) {
            if (comparator.compare(value, high!!) == 1) {
                high = value
            } else if (comparator.compare(value, low!!) == -1) {
                low = value
            }
        } else {
            high = value
            low = value
        }

        priorityQueue.add(value)
        if (priorityQueue.size > numberOfElements) priorityQueue.remove()
    }

    fun get() = priorityQueue.toList()
}