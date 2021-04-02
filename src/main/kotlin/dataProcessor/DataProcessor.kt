package dataProcessor

import util.CommitMapper
import util.FileMapper
import util.UserMapper

interface DataProcessor<D> {
    val userMapper : UserMapper
    val fileMapper : FileMapper
    val commitMapper : CommitMapper

    fun processData(data: D)
    fun calculate()
}
