package uz.coder.foottopbusiness.core.platform

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.NotificationManagerCompat
import uz.coder.foottopbusiness.core.context.ContextProvider

import kotlin.system.exitProcess
import androidx.core.net.toUri

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

actual fun openFile(path: String) {
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
}

actual fun makePhoneCall(phoneNumber: String) {
    val context = ContextProvider.getContext()
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
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
        // We can't easily know if it's permanently denied without trying to request 
        // and checking shouldShowRequestPermissionRationale, which requires Activity.
        // For simplicity, we return DENIED.
        PermissionStatus.DENIED
    }
}

actual suspend fun requestNotificationPermission(): PermissionStatus {
    // On Android, this usually needs an Activity to show the dialog.
    // We will handle the actual request in the UI layer using ActivityResult.
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
