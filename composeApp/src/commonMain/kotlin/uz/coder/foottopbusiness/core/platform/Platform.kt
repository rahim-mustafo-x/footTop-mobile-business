package uz.coder.foottopbusiness.core.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
