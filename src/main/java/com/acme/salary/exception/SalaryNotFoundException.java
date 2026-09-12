package com.acme.salary.exception;

public class SalaryNotFoundException extends RuntimeException {

    private static final String ERROR_CODE = "SALARY_NOT_FOUND";

    public SalaryNotFoundException(String message) {
        super(message);
    }

    public SalaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String getErrorCode() {
        return ERROR_CODE;
    }
}
