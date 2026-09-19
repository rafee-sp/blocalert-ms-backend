package com.blocalert.alert.exception;

public class AlertLimitExceedException extends RuntimeException {

    public AlertLimitExceedException(String message) {
        super(message);
    }
}
