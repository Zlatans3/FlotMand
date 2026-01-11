package dk.zlatan.flotmand.util

import java.text.DateFormatSymbols
import java.util.Locale
import java.util.Calendar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

/**
 * Custom implementation for Danish date formatting and localization.
 * Provides Danish month and weekday names to ensure consistent UI across the app.
 */
object DanishDateFormatter {

    // Danish month names (1-indexed, so index 0 is empty)
    private val danishMonths = arrayOf(
        "", // Placeholder for 0 index
        "Januar",
        "Februar",
        "Marts",
        "April",
        "Maj",
        "Juni",
        "Juli",
        "August",
        "September",
        "Oktober",
        "November",
        "December"
    )

    // Danish month abbreviations
    private val danishMonthsShort = arrayOf(
        "", // Placeholder for 0 index
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "Maj",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Okt",
        "Nov",
        "Dec"
    )

    // Danish weekday names (1-indexed, where 1 = Sunday)
    private val danishWeekdays = arrayOf(
        "", // Placeholder for 0 index
        "Søndag",
        "Mandag",
        "Tirsdag",
        "Onsdag",
        "Torsdag",
        "Fredag",
        "Lørdag"
    )

    // Danish weekday abbreviations
    private val danishWeekdaysShort = arrayOf(
        "", // Placeholder for 0 index
        "Søn",
        "Man",
        "Tir",
        "Ons",
        "Tor",
        "Fre",
        "Lør"
    )

    /**
     * Get the Danish month name for a given month (1-12)
     */
    fun getMonthName(month: Int): String {
        return if (month in 1..12) danishMonths[month] else ""
    }

    /**
     * Get the Danish abbreviated month name for a given month (1-12)
     */
    fun getMonthNameShort(month: Int): String {
        return if (month in 1..12) danishMonthsShort[month] else ""
    }

    /**
     * Get the Danish weekday name for a given day of week
     * @param dayOfWeek Calendar constant (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.)
     */
    fun getWeekdayName(dayOfWeek: Int): String {
        return if (dayOfWeek in 1..7) danishWeekdays[dayOfWeek] else ""
    }

    /**
     * Get the Danish abbreviated weekday name for a given day of week
     * @param dayOfWeek Calendar constant (Calendar.SUNDAY = 1, Calendar.MONDAY = 2, etc.)
     */
    fun getWeekdayNameShort(dayOfWeek: Int): String {
        return if (dayOfWeek in 1..7) danishWeekdaysShort[dayOfWeek] else ""
    }

    /**
     * Format a LocalDate to a Danish readable string format
     * Example: "mandag d. 15. januar 2026"
     */
    fun formatDateDanish(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.value % 7 + 1 // Convert to Calendar format (1=Sunday)
        val dayOfMonth = date.dayOfMonth
        val month = date.monthValue
        val year = date.year

        return "${getWeekdayName(dayOfWeek).lowercase()} d. $dayOfMonth. ${getMonthName(month).lowercase()} $year"
    }

    /**
     * Format a LocalDate to a short Danish format
     * Example: "15. jan 2026"
     */
    fun formatDateDanishShort(date: LocalDate): String {
        val dayOfMonth = date.dayOfMonth
        val month = date.monthValue

        return "$dayOfMonth. ${getMonthNameShort(month).lowercase()}"
    }

    /**
     * Format a LocalDate with only weekday and date
     * Example: "Mandag d. 15. jan"
     */
    fun formatDateDanishWithWeekday(date: LocalDate): String {
        val dayOfWeek = date.dayOfWeek.value % 7 + 1 // Convert to Calendar format (1=Sunday)
        val dayOfMonth = date.dayOfMonth
        val month = date.monthValue

        return "${getWeekdayName(dayOfWeek)} d. $dayOfMonth. ${getMonthNameShort(month).lowercase()}"
    }

    /**
     * Get a DateTimeFormatter that uses Danish locale
     */
    fun getDanishDateFormatter(pattern: String): DateTimeFormatter {
        val danishLocale = Locale("da", "DK")
        return DateTimeFormatter.ofPattern(pattern, danishLocale)
    }
}
