package uz.coder.foottopbusiness.core.platform

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun exitApp() {
    // iOS apps are not supposed to exit programmatically, but if forced:
    // platform.posix.exit(0)
}
