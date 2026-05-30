package uz.coder.foottopbusiness.core.platform

import platform.UIKit.UIDevice
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.StoreKit.SKStoreReviewController
import platform.UserNotifications.*
import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val version: String = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown"
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun exitApp() {
    // iOS apps are not supposed to exit programmatically, but if forced:
    // platform.posix.exit(0)
}

actual fun shareApp(text: String) {
    val activityController = UIActivityViewController(
        activityItems = listOf(text),
        applicationActivities = null
    )
    val window = UIApplication.sharedApplication.keyWindow
    window?.rootViewController?.presentViewController(
        activityController,
        animated = true,
        completion = null
    )
}

actual fun rateApp() {
    SKStoreReviewController.requestReview()
}

actual fun openFile(path: String) {
    // Basic implementation for iOS to open a file
    // In a real app, you might use UIDocumentInteractionController
}

actual fun makePhoneCall(phoneNumber: String) {
    val url = NSURL(string = "tel:$phoneNumber")
    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    }
}

actual fun openAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    }
}

actual suspend fun checkNotificationPermissionStatus(): PermissionStatus = suspendCancellableCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
        val status = when (settings?.authorizationStatus) {
            UNAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
            UNAuthorizationStatusDenied -> PermissionStatus.DENIED
            UNAuthorizationStatusNotDetermined -> PermissionStatus.DENIED
            else -> PermissionStatus.DENIED
        }
        continuation.resume(status)
    }
}

actual suspend fun requestNotificationPermission(): PermissionStatus = suspendCancellableCoroutine { continuation ->
    val center = UNUserNotificationCenter.currentNotificationCenter()
    center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { granted, error ->
        if (granted) {
            continuation.resume(PermissionStatus.GRANTED)
        } else {
            continuation.resume(PermissionStatus.DENIED)
        }
    }
}

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            onResult(requestNotificationPermission())
        }
    }
}

