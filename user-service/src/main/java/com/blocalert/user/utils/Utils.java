package com.blocalert.user.utils;

import org.springframework.util.StringUtils;

public class Utils {

    public static String maskEmail(String email) {

        if (!StringUtils.hasText(email)) return email;

        String[] parts = email.split("@");

        String local = parts[0];
        String domain = parts[1];

        String maskedLocal = local.length() <= 2 ? local : local.substring(0, 2) + "*****";

        return String.join("@", maskedLocal, domain);
    }

}
