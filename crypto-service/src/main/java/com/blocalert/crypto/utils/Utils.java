package com.blocalert.crypto.utils;

public class Utils {

    public static Double nullSafe(Double value) {
        return value == null ? 0.0 : value;
    }

    public static Long nullSafe(Long value) {
        return value == null ? 0L : value;
    }
}
