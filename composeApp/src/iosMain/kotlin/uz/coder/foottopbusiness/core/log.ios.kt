package uz.coder.foottopbusiness.core

import uz.coder.foottopbusiness.core.platform.isDebugBuild

actual fun log(tag: String, message: String?) {
    // Release'da loglar yozilmaydi -- ichida token/shaxsiy ma'lumot bo'lishi mumkin
    if (!isDebugBuild) return
    println("TAG: $tag, MESSAGE: $message")
}