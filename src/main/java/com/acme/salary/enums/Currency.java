package com.acme.salary.enums;

public enum Currency {
    USD("US Dollar"),
    EUR("Euro"),
    GBP("British Pound"),
    INR("Indian Rupee"),
    CAD("Canadian Dollar"),
    AUD("Australian Dollar"),
    SGD("Singapore Dollar"),
    JPY("Japanese Yen");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
