package com.apsbiometria.aps_biometria.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateFormatter {

    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat FORMAT_DATETIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat FORMAT_DATE_SHORT = new SimpleDateFormat("dd/MM/yy");
    private static final SimpleDateFormat FORMAT_ISO = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat FORMAT_ISO_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(Date date) {
        if (date == null)
            return "";
        return FORMAT_DATE.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null)
            return "";
        return FORMAT_DATETIME.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null)
            return "";
        return FORMAT_TIME.format(date);
    }

    public static String formatDateShort(Date date) {
        if (date == null)
            return "";
        return FORMAT_DATE_SHORT.format(date);
    }

    public static String formatISO(Date date) {
        if (date == null)
            return "";
        return FORMAT_ISO.format(date);
    }

    public static String formatISODateTime(Date date) {
        if (date == null)
            return "";
        return FORMAT_ISO_DATETIME.format(date);
    }

    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return FORMAT_DATE.parse(dateStr);
    }

    public static Date parseDateTime(String dateTimeStr) throws ParseException {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return FORMAT_DATETIME.parse(dateTimeStr);
    }

    public static Date parseISO(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return FORMAT_ISO.parse(dateStr);
    }

    public static String now() {
        return formatDateTime(new Date());
    }

    public static String today() {
        return formatDate(new Date());
    }

    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null)
            return 0;

        long diffMillis = end.getTime() - start.getTime();
        return TimeUnit.MILLISECONDS.toDays(diffMillis);
    }

    public static long hoursBetween(Date start, Date end) {
        if (start == null || end == null)
            return 0;

        long diffMillis = end.getTime() - start.getTime();
        return TimeUnit.MILLISECONDS.toHours(diffMillis);
    }

    public static long minutesBetween(Date start, Date end) {
        if (start == null || end == null)
            return 0;

        long diffMillis = end.getTime() - start.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(diffMillis);
    }

    public static String timeAgo(Date date) {
        if (date == null)
            return "";

        long diffMillis = System.currentTimeMillis() - date.getTime();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (seconds < 60) {
            return "agora mesmo";
        } else if (minutes < 60) {
            return minutes + " minuto" + (minutes > 1 ? "s" : "") + " atrás";
        } else if (hours < 24) {
            return hours + " hora" + (hours > 1 ? "s" : "") + " atrás";
        } else if (days < 7) {
            return days + " dia" + (days > 1 ? "s" : "") + " atrás";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " semana" + (weeks > 1 ? "s" : "") + " atrás";
        } else if (days < 365) {
            long months = days / 30;
            return months + " " + (months > 1 ? "meses" : "mês") + " atrás";
        } else {
            long years = days / 365;
            return years + " ano" + (years > 1 ? "s" : "") + " atrás";
        }
    }

    public static Date addDays(Date date, int days) {
        if (date == null)
            return null;
        return new Date(date.getTime() + TimeUnit.DAYS.toMillis(days));
    }

    public static Date addHours(Date date, int hours) {
        if (date == null)
            return null;
        return new Date(date.getTime() + TimeUnit.HOURS.toMillis(hours));
    }

    public static boolean isToday(Date date) {
        if (date == null)
            return false;
        String today = formatDate(new Date());
        String checkDate = formatDate(date);
        return today.equals(checkDate);
    }

    public static boolean isPast(Date date) {
        if (date == null)
            return false;
        return date.before(new Date());
    }

    public static boolean isFuture(Date date) {
        if (date == null)
            return false;
        return date.after(new Date());
    }
}
