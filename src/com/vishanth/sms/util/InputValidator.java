package com.vishanth.sms.util;

import java.util.regex.Pattern;

/**
 * Utility class providing validation routines for student attributes.
 * Enforces data integrity before state mutations.
 */
public class InputValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{7,15}$"
    );

    private static final Pattern ROLL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9-_]{2,20}$"
    );

    public static boolean isValidRollNumber(String roll) {
        return roll != null && ROLL_PATTERN.matcher(roll.trim()).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2 && name.trim().length() <= 50;
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidContactNumber(String contact) {
        if (contact == null) return false;
        String sanitized = contact.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(sanitized).matches();
    }

    public static boolean isValidSemester(int semester) {
        return semester >= 1 && semester <= 12;
    }

    public static boolean isValidAttendance(double attendance) {
        return attendance >= 0.0 && attendance <= 100.0;
    }

    public static boolean isValidMark(double mark) {
        return mark >= 0.0 && mark <= 100.0;
    }

    public static boolean isValidCourse(String course) {
        return course != null && !course.trim().isEmpty() && course.trim().length() <= 60;
    }
}
