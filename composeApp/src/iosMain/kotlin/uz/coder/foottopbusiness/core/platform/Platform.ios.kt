package uz.coder.foottopbusiness.core.platform

import platform.UIKit.UIDevice
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.StoreKit.SKStoreReviewController

import platform.Foundation.NSBundle

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

