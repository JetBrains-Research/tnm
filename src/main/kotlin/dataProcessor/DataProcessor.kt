package dataProcessor


interface DataProcessor<D> {
    fun processData(data: D)
    fun calculate()
}
