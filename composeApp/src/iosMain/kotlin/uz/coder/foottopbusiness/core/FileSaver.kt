package uz.coder.foottopbusiness.core

import platform.Foundation.*

actual fun saveFile(fileName: String, content: String) {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documentsDirectory = urls.firstOrNull() as? NSURL
    val fileURL = documentsDirectory?.URLByAppendingPathComponent(fileName)
    
    val data = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding)
    data?.writeToURL(fileURL!!, true)
}
