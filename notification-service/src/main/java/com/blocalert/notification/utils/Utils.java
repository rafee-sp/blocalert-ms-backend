package com.blocalert.notification.utils;

import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static String formatPrice(BigDecimal price) {

        if (price == null) return "-";

        BigDecimal abs = price.abs();

        DecimalFormat df;
        if (abs.compareTo(BigDecimal.ONE) < 0) { // less than 1 - upto 8
            df = new DecimalFormat("#,##0.########");
        } else {
            df = new DecimalFormat("#,##0.##"); // greater than 1 - upto 2
        }
        return "$" + df.format(price);
    }

    public static String getFormattedCurrDate() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        return LocalDateTime.now().format(formatter);
    }

    public static String formatSubscriptionPrice(long amount) {
        double price = amount / 100.0;
        return String.format("$%.2f", price);
    }

    public static String getAbbreviatedDateFormat(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
    }

    public static String formatPhoneNumber(String phoneNumber, String phoneRegion) {


        if(!StringUtils.hasText(phoneNumber)) throw new IllegalArgumentException("Phone number cannot be blank or null");

        phoneNumber = phoneNumber.replaceAll("\\D", "");

        if(phoneNumber.length() != 10) throw new IllegalArgumentException("Invalid phone number length : "+phoneNumber);

        String countryCode = phoneRegion.equalsIgnoreCase("IN") ? "91" : "1";

        return "+"+ countryCode + phoneNumber;

    }
}
