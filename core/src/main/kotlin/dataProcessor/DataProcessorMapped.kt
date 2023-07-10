package dataProcessor

import dataProcessor.inputData.InputData
import kotlinx.serialization.Serializable
import util.mappers.CommitMapper
import util.mappers.FileMapper
import util.mappers.UserMapper

abstract class DataProcessorMapped<D> : DataProcessor<D> where D : InputData {

    protected val fileMapper = FileMapper()
    val idToFile
        get() = fileMapper.idToFile
    val fileToId
        get() = fileMapper.fileToId

    protected val userMapper = UserMapper()
    val idToUser
        get() = userMapper.idToUser
    val userToId
        get() = userMapper.userToId

    protected val commitMapper = CommitMapper()
    val idToCommit
        get() = commitMapper.idToCommit
    val commitToId
        get() = commitMapper.commitToId

}

