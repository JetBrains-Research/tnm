package dataProcessor

abstract class DataProcessorMappedWithInit<I, D> : DataProcessorMapped<D>() {
    abstract fun init(initData: I)
}