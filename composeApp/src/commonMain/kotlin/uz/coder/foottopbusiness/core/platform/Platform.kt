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

expect fun makePhoneCall(phoneNumber: String)

expect fun openAppSettings()

enum class PermissionStatus {
    GRANTED, DENIED, PERMANENTLY_DENIED
}

expect suspend fun checkNotificationPermissionStatus(): PermissionStatus

expect suspend fun requestNotificationPermission(): PermissionStatus
