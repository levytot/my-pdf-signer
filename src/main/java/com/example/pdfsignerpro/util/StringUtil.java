package com.example.pdfsignerpro.util;

public final class StringUtil {

    private StringUtil() {}

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static int countOccurrences(String text, String pattern) {
        if (isEmpty(text) || isEmpty(pattern)) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}
