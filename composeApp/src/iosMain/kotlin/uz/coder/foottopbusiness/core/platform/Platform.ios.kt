package uz.coder.foottopbusiness.core.platform

import platform.UIKit.UIDevice
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.StoreKit.SKStoreReviewController
import platform.UserNotifications.*
import platform.Foundation.NSURL
import uz.coder.foottopbusiness.core.visualTransformation.normalizePhoneForDial
import platform.Foundation.NSBundle
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.CoreLocation.*
import platform.darwin.NSObject
import platform.Foundation.NSError
import kotlinx.cinterop.*

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val version: String = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "unknown"
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual val isDebugBuild: Boolean
    get() = kotlin.native.Platform.isDebugBinary

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

actual fun openFile(path: String): Boolean {
    // TODO: UIDocumentInteractionController orqali ochish
    // Hozircha qo'llab-quvvatlanmaydi - chaqiruvchi false'ga qarab xabar ko'rsatadi
    return false
}

actual fun makePhoneCall(phoneNumber: String): Boolean {
    val dialNumber = normalizePhoneForDial(phoneNumber) ?: return false
    val url = NSURL(string = "tel:$dialNumber")
    if (!UIApplication.sharedApplication.canOpenURL(url)) return false
    UIApplication.sharedApplication.openURL(url)
    return true
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

actual suspend fun checkLocationPermissionStatus(): PermissionStatus {
    val status = CLLocationManager.authorizationStatus()
    return when (status) {
        kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse -> PermissionStatus.GRANTED
        kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> PermissionStatus.DENIED
        else -> PermissionStatus.DENIED
    }
}

actual suspend fun requestLocationPermission(): PermissionStatus {
    val locationManager = CLLocationManager()
    locationManager.requestWhenInUseAuthorization()
    return checkLocationPermissionStatus()
}

@Composable
actual fun LocationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
) {
    LaunchedEffect(trigger) {
        if (trigger) {
            onResult(requestLocationPermission())
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class LocationDelegate(
    private val onLocationUpdate: (Pair<Double, Double>?) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        if (location != null) {
            location.coordinate.useContents {
                onLocationUpdate(latitude to longitude)
            }
        }
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        onLocationUpdate(null)
    }
    
    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        if (didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedAlways || 
            didChangeAuthorizationStatus == kCLAuthorizationStatusAuthorizedWhenInUse) {
            manager.startUpdatingLocation()
        } else if (didChangeAuthorizationStatus == kCLAuthorizationStatusDenied || 
                   didChangeAuthorizationStatus == kCLAuthorizationStatusRestricted) {
            onLocationUpdate(null)
        }
    }
}

actual suspend fun getCurrentLocation(): Pair<Double, Double>? = suspendCancellableCoroutine { continuation ->
    val locationManager = CLLocationManager()
    val delegate = LocationDelegate { location ->
        locationManager.stopUpdatingLocation()
        if (continuation.isActive) {
            continuation.resume(location)
        }
    }
    locationManager.delegate = delegate
    
    val status = CLLocationManager.authorizationStatus()
    if (status == kCLAuthorizationStatusNotDetermined) {
        locationManager.requestWhenInUseAuthorization()
    } else if (status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse) {
        locationManager.startUpdatingLocation()
    } else {
        continuation.resume(null)
    }

    continuation.invokeOnCancellation {
        locationManager.stopUpdatingLocation()
        locationManager.delegate = null
    }
}
