package uz.coder.foottopbusiness

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform