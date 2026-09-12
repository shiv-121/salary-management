package com.acme.salary.config;

import com.acme.salary.enums.Currency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Static configured assessment FX rates used for analytics comparisons.
 * These are intentionally not live market rates and are intended to be easy to adjust.
 */
@Component
public class CurrencyExchangeRates {

    public static final Currency REPORTING_CURRENCY = Currency.USD;

    private static final Map<Currency, BigDecimal> USD_RATES = buildUsdRates();

    public String getReportingCurrencyCode() {
        return REPORTING_CURRENCY.name();
    }

    public BigDecimal getRate(Currency currency) {
        return USD_RATES.getOrDefault(currency, BigDecimal.ONE);
    }

    public BigDecimal convertToUsd(Currency currency, BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.multiply(getRate(currency));
    }

    public Map<Currency, BigDecimal> getUsdRates() {
        return Collections.unmodifiableMap(USD_RATES);
    }

    private static Map<Currency, BigDecimal> buildUsdRates() {
        EnumMap<Currency, BigDecimal> rates = new EnumMap<>(Currency.class);
        rates.put(Currency.USD, new BigDecimal("1.0"));
        rates.put(Currency.EUR, new BigDecimal("1.08"));
        rates.put(Currency.GBP, new BigDecimal("1.27"));
        rates.put(Currency.INR, new BigDecimal("0.012"));
        rates.put(Currency.CAD, new BigDecimal("0.74"));
        rates.put(Currency.AUD, new BigDecimal("0.66"));
        rates.put(Currency.SGD, new BigDecimal("0.74"));
        rates.put(Currency.JPY, new BigDecimal("0.0067"));
        return rates;
    }
}
