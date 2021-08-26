package util

import java.util.*

class HeapNStorage<T>(private val numberOfElements: Int, private val comparator: Comparator<T>): PriorityQueue<T>(comparator) {
    var low: T? = null
        private set

    var high: T? = null
        private set

    override fun add(element: T): Boolean {
        if (isNotEmpty()) {
            if (comparator.compare(element, high!!) == 1) {
                high = element
            } else if (comparator.compare(element, low!!) == -1) {
                low = element
            }
        } else {
            high = element
            low = element
        }

        val result = offer(element)
        if (size > numberOfElements) remove()

        return result
    }

}
