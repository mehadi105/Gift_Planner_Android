package com.giftplanner.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class DateFormatter {
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    
    public static String formatDateForDisplay(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "";
        }
        try {
            LocalDate date = LocalDate.parse(isoDate, isoFormatter);
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
            return displayFormat.format(calendar.getTime());
        } catch (Exception e) {
            return isoDate;
        }
    }
    
    public static String formatCurrency(double amount) {
        return String.format(Locale.US, "$%.2f", amount);
    }
    
    public static String formatMonthYear(String yearMonth) {
        try {
            String[] parts = yearMonth.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            return monthYearFormat.format(calendar.getTime());
        } catch (Exception e) {
            return yearMonth;
        }
    }
    
    public static String getCurrentYearMonth() {
        LocalDate now = LocalDate.now();
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    
    public static String getMonthStartDate(String yearMonth) {
        return yearMonth + "-01";
    }
    
    public static String getMonthEndDate(String yearMonth) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        return String.format(Locale.US, "%s-%02d", yearMonth, lastDay);
    }
}


