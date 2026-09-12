package com.acme.salary.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeNotFoundException(
            EmployeeNotFoundException ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                EmployeeNotFoundException.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SalaryNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleSalaryNotFoundException(
            SalaryNotFoundException ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                SalaryNotFoundException.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidSalaryException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSalaryException(
            InvalidSalaryException ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                InvalidSalaryException.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "BUSINESS_RULE_VIOLATION",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                "The requested resource was not found",
                ex.getRequestURL()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
