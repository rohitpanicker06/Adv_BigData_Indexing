package com.example.demo1.errors.custom.pattern.factory;


import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

public class ApiResponseFactory {

    public static ApiResponse getEmptyBodyRequestError() {
        return new ApiResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request Body Cannot be Empty");
    }

    public static ApiResponse getSchemaValidationError(Exception e) {
        return new ApiResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase() + "- Request Body does not confine to the required schema",
                e.getMessage());
    }

    public static ApiResponse getUriSyntaxExpcetion() {
        return new ApiResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), " URI syntax exception");
    }

    public static ApiResponse getPlanNotFoundError(String key) {
        return new ApiResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(), "Plan not found", key);
    }

    public static ApiResponse getPlantNotModifiedResponse(String key) {
        return new ApiResponse(
                HttpStatus.NOT_MODIFIED.value(), null,
                HttpStatus.NOT_MODIFIED.getReasonPhrase() ,key);
    }

    public static ApiResponse getInternalServerError() {
        return new ApiResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                null ,null);
    }

    public static ApiResponse getPlanCreatedSuccessfully(String planId) {
        return new ApiResponse(
               HttpStatus.CREATED.value(), null, "plan created successfully!",
                null, planId);
    }

    public static ApiResponse getPlanUpdatedSuccessfully(String planId) {
        return new ApiResponse(
                HttpStatus.CREATED.value(), null, "plan updated successfully!",
                null, planId);
    }

    public static ApiResponse getEtagParsingException(String planId) {
        return new ApiResponse(
                HttpStatus.BAD_REQUEST.value(), null, "ETag value is invalid or not a string",
                null, planId);
    }

    public static ApiResponse getEtagMissing(String planId) {
        return new ApiResponse(
                HttpStatus.BAD_REQUEST.value(), null, "Please provide Etag value",
                null, planId);
    }

    public static ApiResponse getPreConditionFailedForEtag(String planId, String eTag)
    {

        return new ApiResponse(HttpStatus.PRECONDITION_FAILED.value(), null, "Etag does not match",null, planId, eTag);
    }
}
