package com.blocalert.alert.exception;

public class UserServiceUnavailableException extends RuntimeException{

    public UserServiceUnavailableException(String message) {
        super(message);
    }
}
