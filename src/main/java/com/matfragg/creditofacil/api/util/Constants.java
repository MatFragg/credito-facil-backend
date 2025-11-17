package com.matfragg.creditofacil.api.util;

import java.math.BigDecimal;

public class Constants {

    // JWT
    public static final String JWT_SECRET = "mySecretKey";
    public static final long JWT_EXPIRATION = 86400000; // 24 horas

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    // Business Rules
    public static final BigDecimal MIN_MONTHLY_SALARY = new BigDecimal("1130.00");
    public static final int MIN_STOCK_ALERTA = 10;

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // API
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
}