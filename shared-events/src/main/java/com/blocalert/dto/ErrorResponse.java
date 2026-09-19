package com.blocalert.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int statusCode;
    private final Object message;
    private final String errorCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timeStamp;

    public ErrorResponse(int statusCode, Object message) {
        this(statusCode, message, null);
    }

    public ErrorResponse(int statusCode, Object message, String errorCode) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorCode = errorCode;
        this.timeStamp = LocalDateTime.now();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Object getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }
}
/*
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ErrorResponse {

    private int statusCode;
    private Object message;
    private String errorCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timeStamp;

    public ErrorResponse(int statusCode, Object message){
        this.statusCode = statusCode;
        this.message = message;
        this.timeStamp = LocalDateTime.now();
    }

}


 */