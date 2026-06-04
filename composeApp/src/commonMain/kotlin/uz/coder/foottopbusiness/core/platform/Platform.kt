package uz.coder.foottopbusiness.core.platform

import androidx.compose.runtime.Composable

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

expect suspend fun checkLocationPermissionStatus(): PermissionStatus

expect suspend fun requestLocationPermission(): PermissionStatus

@Composable
expect fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
)

@Composable
expect fun LocationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
)

expect suspend fun getCurrentLocation(): Pair<Double, Double>?
