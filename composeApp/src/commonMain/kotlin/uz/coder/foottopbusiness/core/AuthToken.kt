package uz.coder.foottopbusiness.core

/**
 * Server yoki JSON ba'zan tokenni "Bearer xxx" ko'rinishida beradi; Ktor bearerAuth esa
 * yana "Bearer" qo'shadi — natijada "Bearer Bearer xxx" bo'lib 403 chiqadi.
 * Shuning uchun doim faqat haqiqiy JWT/string qismini saqlaymiz va yuboramiz.
 */
private val BEARER_PREFIX = Regex("^(?i)bearer\\s*:?\\s*")

fun normalizeBearerToken(raw: String?): String? {
    if (raw == null) return null
    var t = raw.trim()
    if (t.isEmpty()) return null
    if (t.length >= 2) {
        val q = t.first()
        if ((q == '"' || q == '\'') && t.last() == q) {
            t = t.substring(1, t.length - 1).trim()
        }
    }
    while (true) {
        val m = BEARER_PREFIX.find(t) ?: break
        t = t.removeRange(m.range).trim()
    }
    return t.takeIf { it.isNotEmpty() }
}
