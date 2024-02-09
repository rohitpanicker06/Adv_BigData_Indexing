package com.example.demo1.errors.custom.pattern.strategy;

import com.example.demo1.errors.custom.pattern.factory.ApiResponse;

public interface ErrorHandlingStrategy {
    ApiResponse handle(ApiResponse apiError);
}
