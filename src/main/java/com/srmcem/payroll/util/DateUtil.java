package com.srmcem.payroll.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Stateless utility class for date/time operations used across the application.
 *
 * <p>All methods are {@code static}. Do not instantiate this class.
 *
 * <p>Common formats used in this payroll system:
 * <ul>
 *   <li>{@code DD_MM_YYYY}   – human-readable display dates (e.g. "19-07-2026")</li>
 *   <li>{@code YYYY_MM_DD}   – ISO date for DB / API input  (e.g. "2026-07-19")</li>
 *   <li>{@code MONTH_YEAR}   – payroll period label         (e.g. "July-2026")</li>
 *   <li>{@code DATE_TIME_FMT} – full timestamp for reports  (e.g. "19-07-2026 11:45:00")</li>
 * </ul>
 */
public final class DateUtil {

    // -----------------------------------------------------------------------
    // Formatters
    // -----------------------------------------------------------------------

    public static final DateTimeFormatter DD_MM_YYYY    = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter YYYY_MM_DD    = DateTimeFormatter.ISO_LOCAL_DATE;   // "yyyy-MM-dd"
    public static final DateTimeFormatter MONTH_YEAR    = DateTimeFormatter.ofPattern("MMMM-yyyy");
    public static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private DateUtil() {
        // utility class – no instances
    }

    // -----------------------------------------------------------------------
    // Formatting helpers
    // -----------------------------------------------------------------------

    /** Formats a {@link LocalDate} as {@code dd-MM-yyyy}. */
    public static String format(LocalDate date) {
        return date == null ? null : date.format(DD_MM_YYYY);
    }

    /** Formats a {@link LocalDateTime} as {@code dd-MM-yyyy HH:mm:ss}. */
    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_TIME_FMT);
    }

    /** Formats a {@link YearMonth} as {@code MMMM-yyyy} (e.g. "July-2026"). */
    public static String format(YearMonth yearMonth) {
        return yearMonth == null ? null : yearMonth.format(MONTH_YEAR);
    }

    // -----------------------------------------------------------------------
    // Parsing helpers
    // -----------------------------------------------------------------------

    /** Parses an ISO date string {@code "yyyy-MM-dd"} to {@link LocalDate}. */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, YYYY_MM_DD);
    }

    /** Parses a {@code "MMMM-yyyy"} string to {@link YearMonth}. */
    public static YearMonth parseYearMonth(String yearMonthStr) {
        return YearMonth.parse(yearMonthStr, MONTH_YEAR);
    }

    // -----------------------------------------------------------------------
    // Payroll-specific helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the number of calendar days in the month represented by the
     * given {@link YearMonth}.  Used when computing per-day salary breakdowns.
     */
    public static int daysInMonth(YearMonth yearMonth) {
        return yearMonth.lengthOfMonth();
    }

    /**
     * Returns {@code true} if the given date falls within the payroll period
     * (i.e. within the same year-month).
     */
    public static boolean isInPeriod(LocalDate date, YearMonth period) {
        return YearMonth.from(date).equals(period);
    }

    /** Convenience: returns today's date as a {@code dd-MM-yyyy} string. */
    public static String todayFormatted() {
        return LocalDate.now().format(DD_MM_YYYY);
    }

    /** Returns the current {@link YearMonth}. */
    public static YearMonth currentPeriod() {
        return YearMonth.now();
    }
}
