package com.matfragg.creditofacil.api.util;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    public static boolean isValidDNI(String dni) {
        return dni != null && dni.matches("\\d{8}");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{9,15}");
    }

    public static String sanitizeString(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }
}