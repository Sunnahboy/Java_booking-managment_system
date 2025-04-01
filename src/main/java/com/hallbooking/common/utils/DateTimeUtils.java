package main.java.com.hallbooking.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.swing.JFormattedTextField;
import java.text.ParseException;

public class DateTimeUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Utility methods
    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static LocalTime parseTime(String time) {
        return LocalTime.parse(time, TIME_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static boolean isWithinBusinessHours(LocalTime time) {
        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(18, 0);
        return !time.isBefore(businessStart) && !time.isAfter(businessEnd);
    }

    public static boolean isWeekday(LocalDate date) {
        return date.getDayOfWeek().getValue() <= 5;
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long calculateHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    public static LocalDateTime combineDateAndTime(LocalDate date, String time) {
        LocalTime localTime = parseTime(time);  // Reuse the existing parseTime method
        return LocalDateTime.of(date, localTime);
    }

    public static LocalDateTime roundToNearestHour(LocalDateTime dateTime) {
        return dateTime.withMinute(0).withSecond(0).withNano(0);
    }

    public static boolean isOverlapping(LocalDateTime start1, LocalDateTime end1,
                                        LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public static LocalDateTime getEarliestDateTime(LocalDateTime... dateTimes) {
        LocalDateTime earliest = dateTimes[0];
        for (LocalDateTime dateTime : dateTimes) {
            if (dateTime.isBefore(earliest)) {
                earliest = dateTime;
            }
        }
        return earliest;
    }

    public static LocalDateTime getLatestDateTime(LocalDateTime... dateTimes) {
        LocalDateTime latest = dateTimes[0];
        for (LocalDateTime dateTime : dateTimes) {
            if (dateTime.isAfter(latest)) {
                latest = dateTime;
            }
        }
        return latest;
    }

    // Inner class for date label formatting
    public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
        private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public Object stringToValue(String text) throws ParseException {
            return LocalDate.parse(text, DATE_FORMAT);
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            if (value != null) {
                LocalDate dateValue = (LocalDate) value;
                return dateValue.format(DATE_FORMAT);
            }
            return "";
        }
    }
}
