package dataProcessor

import dataProcessor.initData.InitData
import dataProcessor.inputData.InputData

abstract class DataProcessorMappedWithInit<I, D> : DataProcessorMapped<D>() where D : InputData, I : InitData {
    abstract fun init(initData: I)
}