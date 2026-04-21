package uz.coder.foottopbusiness.core.platform

interface Platform {
    val name: String
    val version: String
}

expect fun getPlatform(): Platform

expect fun exitApp()

expect fun shareApp(text: String)

expect fun rateApp()

expect fun openFile(path: String)
