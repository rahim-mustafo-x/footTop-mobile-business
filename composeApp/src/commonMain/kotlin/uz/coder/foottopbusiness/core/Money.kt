package uz.coder.foottopbusiness.core

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Pul summalarini formatlash uchun yagona joy.
 *
 * Ilgari har bir ekran o'zicha formatlar edi: bir joyda "2500000",
 * boshqasida "2M UZS", uchinchisida "2500K". Endi hammasi shu yerdan.
 */
object Money {

    private const val NBSP = ' ' // uzilmas probel - raqam qatorlar orasida bo'linmasin

    /**
     * To'liq summa, razryadlari ajratilgan: 1500000.0 -> "1 500 000"
     */
    fun format(amount: Double): String {
        val rounded = amount.roundToLong()
        val negative = rounded < 0
        val digits = abs(rounded).toString()

        val grouped = buildString {
            digits.forEachIndexed { index, char ->
                // Boshidan emas, oxiridan uchtalab ajratamiz
                if (index > 0 && (digits.length - index) % 3 == 0) append(NBSP)
                append(char)
            }
        }
        return if (negative) "-$grouped" else grouped
    }

    /**
     * Qisqartirilgan ko'rinish - kartalar va statistikada joy tor bo'lganda:
     * 1500000.0 -> "1,5M", 250000.0 -> "250K", 900.0 -> "900"
     */
    fun compact(amount: Double): String {
        val negative = amount < 0
        val value = abs(amount)

        val result = when {
            value >= 1_000_000 -> "${trimDecimal(value / 1_000_000)}M"
            value >= 1_000 -> "${trimDecimal(value / 1_000)}K"
            else -> value.roundToLong().toString()
        }
        return if (negative) "-$result" else result
    }

    /**
     * Valyuta bilan to'liq summa: 1500000.0 -> "1 500 000 so'm"
     * [currency] ni Localization.current.currency dan uzating.
     */
    fun withCurrency(amount: Double, currency: String): String =
        "${format(amount)}$NBSP$currency"

    /**
     * Valyuta bilan qisqartirilgan summa: 1500000.0 -> "1,5M so'm"
     */
    fun compactWithCurrency(amount: Double, currency: String): String =
        "${compact(amount)}$NBSP$currency"

    /**
     * Bir xona kasr qoldiradi, lekin butun son bo'lsa kasrni tashlaydi:
     * 1.5 -> "1,5", 2.0 -> "2"
     */
    private fun trimDecimal(value: Double): String {
        val oneDecimal = (value * 10).roundToLong()
        val whole = oneDecimal / 10
        val fraction = oneDecimal % 10
        return if (fraction == 0L) whole.toString() else "$whole,$fraction"
    }
}
