package uz.coder.foottopbusiness.core

import android.os.Environment
import java.io.File
import java.io.FileOutputStream

actual fun saveFile(fileName: String, content: String) {
    try {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsFolder, fileName)
        FileOutputStream(file).use { output ->
            output.write(content.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
