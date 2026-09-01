package com.ecommerce.automation.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Parses raw currency-formatted price strings (e.g. "₹52,999.00") into BigDecimal.
public final class PriceUtils {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("[0-9]+(?:,[0-9]{2,3})*(?:\\.[0-9]+)?");

    private PriceUtils() {
    }

    public static BigDecimal parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }
        Matcher matcher = NUMERIC_PATTERN.matcher(rawText);
        if (!matcher.find()) {
            return null;
        }
        String numeric = matcher.group().replace(",", "");
        try {
            return new BigDecimal(numeric).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean areEqual(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.compareTo(b) == 0;
    }

    public static String format(BigDecimal amount) {
        return amount == null ? "N/A" : "₹" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
