package uz.coder.foottopbusiness.core

import android.os.Process

actual fun exitApp() {
    Process.killProcess(Process.myPid())
}