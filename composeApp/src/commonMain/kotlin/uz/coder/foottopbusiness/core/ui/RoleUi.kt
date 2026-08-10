package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.coder.foottopbusiness.core.localization.Language
import uz.coder.foottopbusiness.core.localization.Localization
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.domain.model.UserRole

/**
 * Rolning UI ko'rinishi - nom, ta'rif, rang va ikonka.
 *
 * Ilgari rol nomlari ekranlar ichida qattiq yozilgan ("Stadion egasi",
 * "Coach"), UI'da esa rol umuman ko'rsatilmasdi - foydalanuvchi o'zining
 * qaysi rolda ekanini bilolmasdi. Endi hamma joy shu yerdan oladi.
 */

fun UserRole.displayName(strings: Language): String = when (this) {
    UserRole.SUPER_ADMIN -> strings.roleSuperAdmin
    UserRole.DISTRICT_ADMIN -> strings.roleDistrictAdmin
    UserRole.OWNER -> strings.roleOwner
    UserRole.COACH -> strings.roleCoach
    UserRole.PLAYER -> strings.rolePlayer
    UserRole.UNKNOWN -> strings.roleUnknown
}

fun UserRole.description(strings: Language): String = when (this) {
    UserRole.SUPER_ADMIN -> strings.roleSuperAdminDesc
    UserRole.DISTRICT_ADMIN -> strings.roleDistrictAdminDesc
    UserRole.OWNER -> strings.roleOwnerDesc
    UserRole.COACH -> strings.roleCoachDesc
    UserRole.PLAYER -> strings.rolePlayerDesc
    UserRole.UNKNOWN -> ""
}

/**
 * Har bir rolga o'z rangi - nishoncha bir qarashda ajralib tursin.
 * Ranglar tema'dan mustaqil: rol identifikatori ikkala temada ham bir xil
 * o'qilishi kerak.
 */
val UserRole.accentColor: Color
    get() = when (this) {
        UserRole.SUPER_ADMIN -> Color(0xFFDC2626)   // qizil - eng yuqori vakolat
        UserRole.DISTRICT_ADMIN -> Color(0xFFF59E0B) // sariq - tuman darajasi
        UserRole.OWNER -> Color(0xFF10B981)          // yashil - biznes egasi
        UserRole.COACH -> Color(0xFF3B82F6)          // ko'k - murabbiy
        UserRole.PLAYER -> Color(0xFF8B5CF6)         // binafsha - o'yinchi
        UserRole.UNKNOWN -> Color(0xFF9CA3AF)        // kulrang
    }

val UserRole.icon: ImageVector
    get() = when (this) {
        UserRole.SUPER_ADMIN -> Icons.Outlined.Shield
        UserRole.DISTRICT_ADMIN -> Icons.Outlined.AdminPanelSettings
        UserRole.OWNER -> Icons.Outlined.Home
        UserRole.COACH -> Icons.Outlined.Sports
        UserRole.PLAYER -> Icons.Outlined.SportsSoccer
        UserRole.UNKNOWN -> Icons.AutoMirrored.Outlined.HelpOutline
    }

/**
 * Rolning ish ko'lami - "men qaysi doirada ishlayapman" degan savolga javob.
 *
 * Super admin bilan tuman admini UI'da aynan bir xil ko'rinardi; ularni
 * ajratadigan yagona narsa shu ko'lam.
 *
 * Muhim: bu ekranga tushadigan rollar uchun natija hech qachon null bo'lmasligi
 * kerak. Aks holda ma'lumot kelganda sarlavha ostida yangi qator paydo bo'lib,
 * header balandligi sakrab ketadi. Shuning uchun statistika hali kelmagan
 * bo'lsa ham o'rnini rol ta'rifi to'ldirib turadi.
 *
 * @param stadiumCount egaga tegishli stadionlar soni (faqat [UserRole.OWNER] uchun)
 */
fun UserRole.scopeText(strings: Language, user: UserDto?, stadiumCount: Int = 0): String? = when (this) {
    UserRole.SUPER_ADMIN -> strings.scopeWholeSystem
    UserRole.DISTRICT_ADMIN -> user?.districtName?.takeIf { it.isNotBlank() } ?: strings.scopeNoDistrict
    // "0 ta stadion" chaqnab ketmasin - son kelgunicha rol ta'rifi turadi
    UserRole.OWNER -> if (stadiumCount > 0) strings.stadiumCount(stadiumCount) else strings.roleOwnerDesc
    UserRole.COACH -> strings.roleCoachDesc
    // Bu rollar bosh sahifaga tushmaydi
    UserRole.PLAYER, UserRole.UNKNOWN -> null
}

/**
 * Rol nishonchasi - ism yonida turadigan kichik chip.
 *
 * @param onGradient gradient header ustida turganda true - fon yarim shaffof
 *   oq bo'ladi, aks holda rolning o'z rangi ishlatiladi
 */
@Composable
fun RoleBadge(
    role: UserRole,
    modifier: Modifier = Modifier,
    onGradient: Boolean = false
) {
    val strings = Localization.current
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    val background = if (onGradient) onPrimary.copy(alpha = 0.18f) else role.accentColor.copy(alpha = 0.12f)
    val content = if (onGradient) onPrimary else role.accentColor

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                role.icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(14.dp)
            )
            Text(
                role.displayName(strings).uppercase(),
                color = content,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}
