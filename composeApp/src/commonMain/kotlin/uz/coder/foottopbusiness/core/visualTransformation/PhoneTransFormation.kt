package uz.coder.foottopbusiness.core.visualTransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Telefon raqamida ko'rsatiladigan maksimal raqamlar soni. */
private const val PhoneDigits = 9

/**
 * Telefon raqamini "(99) 123-45-67" ko'rinishida formatlaydi.
 *
 * Offset xaritasi HAQIQIY matn uzunligidan hisoblanadi.
 *
 * Ilgari xarita qattiq chegaralar bilan yozilgan edi
 * (`if (offset <= 4) return offset - 1`) va faqat to'liq 9 xonali raqam uchun
 * to'g'ri ishlardi. Qisqaroq matnda Compose diapazondan tashqari qiymat olib,
 * ilovani yiqitardi:
 * "OffsetMapping.transformedToOriginal returned invalid mapping: 4 -> 3
 *  is not in range of original text [0, 2]"
 *
 * @param prefix formatlangan matn oldiga qo'shiladigan qism. Maydonning o'zida
 *   `prefix = { Text("+998 ") }` bo'lsa, bu yerda bo'sh qoldiring - aks holda
 *   kod ikki marta chiqadi.
 */
class PhoneTransformation(private val prefix: String = "") : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val out = StringBuilder(prefix)

        // map[i] = asl matndagi i-belgidan oldingi kursor pozitsiyasining
        // formatlangan matndagi o'rni. Uzunligi original.length + 1 -
        // oxirgi element kursor matn oxirida turgan holat uchun.
        val map = IntArray(original.length + 1)

        for (i in original.indices) {
            if (i >= PhoneDigits) {
                // 9 tadan ortiq belgilar ko'rsatilmaydi, lekin ular uchun ham
                // xaritada o'rin bo'lishi shart
                map[i] = out.length
                continue
            }
            if (i == 0) out.append("(")
            map[i] = out.length
            out.append(original[i])
            if (i == 1) out.append(") ")
            if (i == 4) out.append("-")
            if (i == 6) out.append("-")
        }
        map[original.length] = out.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                map[offset.coerceIn(0, original.length)]

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, out.length)
                // clamped'dan oldinda nechta asl belgi borligini topamiz
                var result = 0
                for (i in 0..original.length) {
                    if (map[i] <= clamped) result = i else break
                }
                return result
            }
        }

        return TransformedText(AnnotatedString(out.toString()), offsetMapping)
    }
}

/** "+998 " kodi bilan to'liq ko'rinish: "+998 (99) 123-45-67". */
fun phoneTransformationWithCode() = PhoneTransformation(prefix = "+998 ")

/**
 * Raqamni `tel:` URI uchun normallashtiradi.
 *
 * Backend telefonni turli ko'rinishda saqlaydi: "992314567" (9 xona) yoki
 * "998995083767" (kod bilan). `tel:` URI'siga esa to'liq, xalqaro formatdagi
 * raqam kerak - aks holda Intent'ga hech qaysi ilova javob bermaydi.
 *
 * @return "+998901234567" ko'rinishida, yoki raqam yaroqsiz bo'lsa null
 */
fun normalizePhoneForDial(phone: String?): String? {
    val digits = phone?.filter { it.isDigit() }.orEmpty()
    return when {
        digits.isEmpty() -> null
        digits.length == 9 -> "+998$digits"
        digits.startsWith("998") -> "+$digits"
        else -> "+$digits"
    }
}

fun formatPhoneNumber(phone: String?): String {
    if (phone.isNullOrBlank()) return "+998 (__) ___-__-__"
    val digits = phone.filter { it.isDigit() }
    val cleanDigits = when {
        digits.length == 12 && digits.startsWith("998") -> digits.substring(3)
        digits.length == 9 -> digits
        else -> return phone
    }

    return "+998 (${cleanDigits.substring(0, 2)}) ${cleanDigits.substring(2, 5)}-" +
        "${cleanDigits.substring(5, 7)}-${cleanDigits.substring(7, 9)}"
}
