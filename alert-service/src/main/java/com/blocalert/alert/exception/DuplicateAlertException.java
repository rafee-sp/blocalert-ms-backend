package com.blocalert.alert.exception;

public class DuplicateAlertException extends RuntimeException{

    public DuplicateAlertException(String message) {
        super(message);
    }
}
