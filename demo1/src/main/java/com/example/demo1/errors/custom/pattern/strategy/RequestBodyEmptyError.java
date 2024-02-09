package com.example.demo1.errors.custom.pattern.strategy;

import com.example.demo1.errors.custom.pattern.factory.ApiResponse;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


public class RequestBodyEmptyError implements ErrorHandlingStrategy {
    @Override
    public ApiResponse handle(ApiResponse apiError) {
        apiError.setStatus(400);
        apiError.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        apiError.setMessage("Request Body is Empty");
        apiError.setTimestamp(LocalDateTime.now());
        return apiError;
    }
}
