package com.gvp.financialdashboard.domain.dto;

import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        List<FieldError> fields
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), null);
    }

    public static ErrorResponse ofFields(int status, String error, String message, List<FieldError> fields) {
        return new ErrorResponse(status, error, message, LocalDateTime.now(), fields);
    }
}
