package com.acme.salary.exception;

public class InvalidSalaryException extends RuntimeException {

    private static final String ERROR_CODE = "INVALID_SALARY";

    public InvalidSalaryException(String message) {
        super(message);
    }

    public InvalidSalaryException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String getErrorCode() {
        return ERROR_CODE;
    }
}
