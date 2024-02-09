package com.example.demo1.errors.custom.pattern.factory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private int status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private LocalDateTime timestamp;
    private String error;
    private String message;

    private String key;
    private String planId;


    public ApiResponse() {
        timestamp = LocalDateTime.now();
    }

    public ApiResponse(int status) {
        this();
        this.status = status;
    }

    public ApiResponse(int status, Throwable ex) {
        this();
        this.status = status;
        this.error = "Unexpected error";
        this.message = ex.getLocalizedMessage();
    }

    public ApiResponse(int status, String error, Throwable ex) {
        this();
        this.status = status;
        this.error = error;
        this.message = ex.getLocalizedMessage();
    }

    public ApiResponse(int status, String error, String debugMessage) {
        this();
        this.status = status;
        this.error = error;
        this.message = debugMessage;
    }
    public ApiResponse(int status, String error, String debugMessage, String key) {
        this();
        this.status = status;
        this.error = error;
        this.message = debugMessage;
        this.key = key;
    }

    public ApiResponse(int status, String error, String debugMessage, String key, String objectId) {
        this();
        this.status = status;
        this.error = error;
        this.message = debugMessage;
        this.key = key;
        this.planId = objectId;
    }

    //this was done for for implementing strategy pattern if needed.
//    public ApiError applyStrategy(ErrorHandlingStrategy strategy) {
//        return strategy.handle(this);
//    }
}
