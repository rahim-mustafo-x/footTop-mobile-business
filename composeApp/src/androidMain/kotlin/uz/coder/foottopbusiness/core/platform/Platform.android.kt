package uz.coder.foottopbusiness.core.platform

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import uz.coder.foottopbusiness.core.context.ContextProvider
import uz.coder.foottopbusiness.core.visualTransformation.normalizePhoneForDial
import kotlin.system.exitProcess

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val version: String = try {
        val context = ContextProvider.getContext()
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "unknown"
    } catch (_: Exception) {
        "unknown"
    }
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val isDebugBuild: Boolean
    get() = try {
        val context = ContextProvider.getContext()
        (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    } catch (_: Exception) {
        false
    }

actual fun exitApp() {
    exitProcess(0)
}

actual fun shareApp(text: String) {
    val context = ContextProvider.getContext()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Share via").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

actual fun rateApp() {
    val context = ContextProvider.getContext()
    val packageName = context.packageName
    val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
    }
}

actual fun openFile(path: String): Boolean {
    return try {
        val context = ContextProvider.getContext()
        val file = java.io.File(path)
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        // Qurilmada CSV ochadigan ilova bo'lmasligi mumkin (emulyatorda odatda yo'q).
        // Ilgari bu ActivityNotFoundException'ni hech kim ushlamasdi va ilova yiqilardi.
        false
    }
}

actual fun makePhoneCall(phoneNumber: String): Boolean {
    // "tel:" sxemasi shart. Ilgari xom raqam toUri() qilingani uchun Intent
    // ma'lumotsiz qolib, ActivityNotFoundException bilan ilova yiqilardi.
    val dialNumber = normalizePhoneForDial(phoneNumber) ?: return false
    return try {
        val context = ContextProvider.getContext()
        val intent = Intent(Intent.ACTION_DIAL, "tel:$dialNumber".toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        // Qurilmada telefon ilovasi bo'lmasligi mumkin (ba'zi emulyatorlarda)
        false
    }
}

actual fun openAppSettings() {
    val context = ContextProvider.getContext()
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

actual suspend fun checkNotificationPermissionStatus(): PermissionStatus {
    val context = ContextProvider.getContext()
    return if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
        PermissionStatus.GRANTED
    } else {
        PermissionStatus.DENIED
    }
}

actual suspend fun requestNotificationPermission(): PermissionStatus {
    return checkNotificationPermissionStatus()
}

@Composable
actual fun NotificationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            onResult(if (isGranted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
        }

        LaunchedEffect(trigger) {
            if (trigger) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        LaunchedEffect(trigger) {
            if (trigger) {
                onResult(PermissionStatus.GRANTED)
            }
        }
    }
}

@Composable
actual fun LocationPermissionLauncher(
    trigger: Boolean,
    onResult: (PermissionStatus) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)
        onResult(if (granted) PermissionStatus.GRANTED else PermissionStatus.DENIED)
    }

    LaunchedEffect(trigger) {
        if (trigger) {
            launcher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

actual suspend fun checkLocationPermissionStatus(): PermissionStatus {
    val context = ContextProvider.getContext()
    val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    return if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
        PermissionStatus.GRANTED
    } else {
        PermissionStatus.DENIED
    }
}

actual suspend fun requestLocationPermission(): PermissionStatus {
    return checkLocationPermissionStatus()
}

@SuppressLint("MissingPermission")
actual suspend fun getCurrentLocation(): Pair<Double, Double>? {
    val context = ContextProvider.getContext()
    if (checkLocationPermissionStatus() != PermissionStatus.GRANTED) return null
    
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // lastLocation ko'p holatda null qaytaradi: yangi qurilma, emulyator yoki GPS
    // uzoq vaqt ishlatilmagan bo'lsa kesh bo'sh bo'ladi. Ilgari shunda xarita jim
    // turardi va joylashuv umuman tanlanmasdi, shuning uchun kesh bo'sh chiqsa
    // qurilmadan yangi fix so'raymiz.
    val cached = try {
        fusedLocationClient.lastLocation.await()
    } catch (_: Exception) {
        null
    }
    if (cached != null) return cached.latitude to cached.longitude

    val cancellationSource = CancellationTokenSource()
    return try {
        withTimeoutOrNull(FRESH_LOCATION_TIMEOUT_MS) {
            fusedLocationClient.getCurrentLocation(
                CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(FRESH_LOCATION_MAX_AGE_MS)
                    .build(),
                cancellationSource.token
            ).await()
        }?.let { it.latitude to it.longitude }
    } catch (_: Exception) {
        null
    } finally {
        // Timeout yoki xatoda GPS so'rovi osilib qolmasligi uchun.
        cancellationSource.cancel()
    }
}

/** Yangi GPS fix'ini kutish muddati. Ochiq havoda odatda 2-5 soniya. */
private const val FRESH_LOCATION_TIMEOUT_MS = 15_000L

/** Shu yoshdagi fix yetarli, GPS'ni qaytadan yoqib o'tirmaymiz. */
private const val FRESH_LOCATION_MAX_AGE_MS = 60_000L
