package dataProcessor

import util.CommitMapper
import util.FileMapper
import util.UserMapper

abstract class DataProcessorMapped<T> : DataProcessor<T> {

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

