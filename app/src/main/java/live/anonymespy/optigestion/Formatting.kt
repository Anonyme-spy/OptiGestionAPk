package live.anonymespy.optigestion

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/** "1234567.0" -> "$1,234,567" (or "1,234,567 Ar", etc, depending on the selected currency).
 * "1234567.0" -> "$1,234,567" (ou "1,234,567 Ar", etc, selon la devise sélectionnée). */
fun formatCurrency(amount: Double): String {
    val rounded = amount.roundToInt()
    val negative = rounded < 0
    val text = abs(rounded).toString().reversed().chunked(3).joinToString(",").reversed()
    val currency = AppRepository.currency
    val sign = if (negative) "-" else ""
    return if (currency.symbolAfter) "$sign$text ${currency.symbol}" else "$sign${currency.symbol}$text"
}

/** Compact form for tight spaces, e.g. "868K", "2.4M".
 * Forme compacte pour les espaces restreints, ex: "868K", "2.4M". */
fun formatCurrencyCompact(amount: Double): String {
    val a = abs(amount)
    val sign = if (amount < 0) "-" else ""
    val currency = AppRepository.currency
    fun withSymbol(number: String) = if (currency.symbolAfter) "$number ${currency.symbol}" else "${currency.symbol}$number"
    return when {
        a >= 1_000_000 -> sign + withSymbol(trimTrailingZero(a / 1_000_000.0) + "M")
        a >= 1_000 -> sign + withSymbol((a / 1_000.0).roundToInt().toString() + "K")
        else -> formatCurrency(amount)
    }
}

private fun trimTrailingZero(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

fun formatPercent(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

/** "3.5" months, "2" months — one decimal, trimmed. Used by the runway KPI.
 * "3,5" mois, "2" mois — une décimale, ajustée. Utilisé par le KPI de piste. */
fun formatMonths(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

/** "Today, 09:41 AM" / "Yesterday" / "12 Jun 2026" style label from a timestamp.
 * Étiquette de style "Aujourd'hui, 09:41 AM" / "Hier" / "12 juin 2026" à partir d'un timestamp. */
fun formatDateLabel(timestampMillis: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val timeFmt = SimpleDateFormat("h:mm a", Locale.FRENCH)
    return when {
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) ->
            "Aujourd'hui, ${timeFmt.format(Date(timestampMillis))}"
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1 ->
            "Hier"
        else -> SimpleDateFormat("d MMM yyyy", Locale.FRENCH).format(Date(timestampMillis))
    }
}

/** Short month label ("Juil") for a timestamp, used to bucket the profitability trend.
 * Étiquette de mois courte ("Juil") pour un timestamp, utilisée pour regrouper la tendance de rentabilité. */
fun monthKeyAndLabel(timestampMillis: Long): Pair<String, String> {
    val cal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
    val label = SimpleDateFormat("MMM", Locale.FRENCH).format(Date(timestampMillis)).replaceFirstChar { it.uppercase() }
    return key to label
}

fun currentPeriodLabel(): String {
    val cal = Calendar.getInstance()
    val quarter = (cal.get(Calendar.MONTH) / 3) + 1
    return "T$quarter ${cal.get(Calendar.YEAR)}"
}

/** Start-of-period timestamp for a [PeriodFilter], or null for [PeriodFilter.ALL] (no lower bound).
 * Timestamp de début de période pour un [PeriodFilter], ou null pour [PeriodFilter.ALL] (pas de limite inférieure). */
fun periodFilterStartMillis(filter: PeriodFilter): Long? {
    if (filter == PeriodFilter.ALL) return null
    val cal = Calendar.getInstance()
    when (filter) {
        PeriodFilter.MONTH -> cal.set(Calendar.DAY_OF_MONTH, 1)
        PeriodFilter.QUARTER -> {
            val quarterStartMonth = (cal.get(Calendar.MONTH) / 3) * 3
            cal.set(Calendar.MONTH, quarterStartMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        PeriodFilter.YEAR -> {
            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        PeriodFilter.ALL -> {}
    }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
