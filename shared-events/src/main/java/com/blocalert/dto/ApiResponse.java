package com.blocalert.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private final String message;
    private final Object data;
    private final PaginationInfo page;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime localDateTime;

    public ApiResponse(String message, Object data) {
        this(message, data, null);
    }

    public ApiResponse(String message, Object data, PaginationInfo page) {
        this.message = message;
        this.data = data;
        this.page = page;
        this.localDateTime = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public PaginationInfo getPage() {
        return page;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }
}

/*

    private String message;
    private Object data;
    private PaginationInfo page;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime localDateTime;

    public ApiResponse(String message, Object data) {
        this.message = message;
        this.data = data;
        this.localDateTime = LocalDateTime.now();
    }

    public ApiResponse(String message, Object data, Page<?> page) {
        this.message = message;
        this.data = data;
        this.page = page != null ? new PaginationInfo(page) : null;
        this.localDateTime = LocalDateTime.now();
    }
}


 */