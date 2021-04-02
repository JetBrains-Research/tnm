package dataProcessor

interface DataProcessorWithInit<I, D> : DataProcessor<D> {
    fun init(initData: I)
}