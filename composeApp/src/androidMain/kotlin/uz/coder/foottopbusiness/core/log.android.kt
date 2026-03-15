package uz.coder.foottopbusiness.core

import android.util.Log

actual fun log(tag: String, message: String?) {
    Log.d(tag, message?:"")
}