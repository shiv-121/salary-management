package com.acme.salary.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path
) {
}
