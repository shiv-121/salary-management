package com.acme.salary.exception;

public class EmployeeNotFoundException extends RuntimeException {

    private static final String ERROR_CODE = "EMPLOYEE_NOT_FOUND";

    public EmployeeNotFoundException(String message) {
        super(message);
    }

    public EmployeeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String getErrorCode() {
        return ERROR_CODE;
    }
}
