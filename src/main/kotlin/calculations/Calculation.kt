package calculations

import java.io.File

interface Calculation {
    fun run()
    fun saveToJson(resourceDirectory: File)
}