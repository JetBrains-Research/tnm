package dataProcessor

/**
 * Interface for processing incoming data.
 * All research techniques use different types of data. To unify such cases, this interface got generic type D,
 * which stands for data type needed for each technique.
 */

interface DataProcessor<D> {
    fun processData(data: D)
    fun calculate()
}
