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
import kotlinx.coroutines.withTimeoutOrNull
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

/**
 * Foydalanuvchi ruxsat dialogiga javob berguncha kutadi. Ilgari so'rov yuborilgan zahoti
 * holat (hali NotDetermined, ya'ni DENIED) qaytarilardi, CLLocationManager esa darhol
 * yo'q qilinib, dialog ham yopilib ketishi mumkin edi.
 */
actual suspend fun requestLocationPermission(): PermissionStatus {
    if (CLLocationManager.authorizationStatus() != kCLAuthorizationStatusNotDetermined) {
        return checkLocationPermissionStatus()
    }
    return suspendCancellableCoroutine { continuation ->
        val locationManager = CLLocationManager()
        val delegate = AuthorizationDelegate { status ->
            if (status != kCLAuthorizationStatusNotDetermined && continuation.isActive) {
                locationManager.delegate = null
                continuation.resume(
                    if (status == kCLAuthorizationStatusAuthorizedAlways ||
                        status == kCLAuthorizationStatusAuthorizedWhenInUse
                    ) PermissionStatus.GRANTED else PermissionStatus.DENIED
                )
            }
        }
        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        // Handler manager va delegate'ni javob kelguncha tirik ushlab turadi (delegate weak)
        continuation.invokeOnCancellation {
            locationManager.delegate = null
            delegate.hashCode()
        }
    }
}

private class AuthorizationDelegate(
    private val onStatus: (CLAuthorizationStatus) -> Unit
) : NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        onStatus(didChangeAuthorizationStatus)
    }
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
            // Bir martalik aniq joylashuv (startUpdatingLocation birinchi bo'lib eski keshni berardi)
            manager.requestLocation()
        } else if (didChangeAuthorizationStatus == kCLAuthorizationStatusDenied ||
                   didChangeAuthorizationStatus == kCLAuthorizationStatusRestricted) {
            onLocationUpdate(null)
        }
    }
}

actual suspend fun getCurrentLocation(): Pair<Double, Double>? = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
    suspendCancellableCoroutine { continuation ->
        val locationManager = CLLocationManager()
        locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
        val delegate = LocationDelegate { location ->
            if (continuation.isActive) {
                locationManager.delegate = null
                continuation.resume(location)
            }
        }
        locationManager.delegate = delegate

        val status = CLLocationManager.authorizationStatus()
        when (status) {
            // Ruxsat berilgach delegate requestLocation() ni o'zi chaqiradi
            kCLAuthorizationStatusNotDetermined -> locationManager.requestWhenInUseAuthorization()
            kCLAuthorizationStatusAuthorizedAlways, kCLAuthorizationStatusAuthorizedWhenInUse ->
                locationManager.requestLocation()
            else -> continuation.resume(null)
        }

        // Handler manager va delegate'ni natija kelguncha tirik ushlaydi (delegate weak).
        // Ilgari delegate GC bo'lib ketsa, "Joylashuv aniqlanmoqda" abadiy qolardi.
        continuation.invokeOnCancellation {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
            delegate.hashCode()
        }
    }
}

/** Joylashuvni kutish muddati. Bino ichida GPS uzoq cho'zilishi mumkin. */
private const val LOCATION_TIMEOUT_MS = 15_000L
