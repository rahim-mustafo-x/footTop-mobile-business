package uz.coder.foottopbusiness.core

actual fun log(tag: String, message: String?) {
    println("TAG: $tag, MESSAGE: $message")
}