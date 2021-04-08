package miners

import dataProcessor.DataProcessor

interface Miner<T> where T : DataProcessor<*> {
    fun run(dataProcessor: T)
}