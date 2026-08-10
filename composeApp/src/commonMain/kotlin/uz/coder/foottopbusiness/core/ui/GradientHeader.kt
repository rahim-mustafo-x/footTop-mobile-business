package uz.coder.foottopbusiness.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Barcha gradient header'lar uchun yagona burchak radiusi. */
private val HeaderCornerRadius = 28.dp

/**
 * Ilovaning asosiy ekran sarlavhasi - gradientli, pastki burchaklari yumaloq.
 *
 * Ilgari har bir ekran shu blokni o'zi qayta yozardi va ular bir-biridan
 * farq qilib ketgan edi (radius 32 vs 36, pastki padding 24 vs 32, ba'zisida
 * Color.White qattiq yozilgan). Endi hammasi shu komponentdan foydalanadi.
 *
 * @param overline sarlavha ustidagi kichik yozuv (masalan "XUSH KELIBSIZ")
 * @param badge sarlavha ustidagi komponent - [RoleBadge] uchun. Berilsa,
 *   [overline] o'rniga shu ko'rsatiladi
 * @param subtitle sarlavha ostidagi izoh
 * @param onBack null bo'lmasa, chapda orqaga tugmasi chiqadi
 * @param actions o'ng tomondagi tugmalar - [HeaderIconButton] ishlating
 */
@Composable
fun GradientHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    badge: @Composable (() -> Unit)? = null,
    subtitle: String? = null,
    titleFontSize: TextUnit = 21.sp,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = HeaderCornerRadius, bottomEnd = HeaderCornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(
                top = statusBarPadding + 12.dp,
                // Orqaga tugmasining o'z ichki bo'shlig'i bor, shuning uchun chapdan kamroq
                start = if (onBack != null) 12.dp else 20.dp,
                end = 20.dp,
                bottom = 22.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                if (badge != null) {
                    badge()
                    Spacer(Modifier.height(6.dp))
                } else if (overline != null) {
                    Text(
                        overline,
                        color = onPrimary.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                Text(
                    title,
                    color = onPrimary,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        color = onPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/**
 * [GradientHeader] ichidagi tugma - gradient fonda yarim shaffof kvadrat yoki doira.
 *
 * @param shape doira kerak bo'lsa CircleShape uzating
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        modifier = modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
