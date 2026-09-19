package com.blocalert.notification.exception;

public class UserServiceUnavailableException extends RuntimeException{

    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
