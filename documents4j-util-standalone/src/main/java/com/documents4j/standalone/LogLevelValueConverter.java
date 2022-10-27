package com.documents4j.standalone;

import ch.qos.logback.classic.Level;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

class LogLevelValueConverter implements ValueConverter<Level> {

    private static final String LEVEL_OFF = "off";
    private static final String LEVEL_ERROR = "error";
    private static final String LEVEL_WARN = "warn";
    private static final String LEVEL_INFO = "info";
    private static final String LEVEL_DEBUG = "debug";
    private static final String LEVEL_TRACE = "trace";
    private static final String LEVEL_ALL = "all";

    @Override
    public Level convert(String value) {
        value = value.toLowerCase();
        switch (value) {
            case LEVEL_OFF:
                return Level.OFF;
            case LEVEL_ERROR:
                return Level.ERROR;
            case LEVEL_WARN:
                return Level.WARN;
            case LEVEL_INFO:
                return Level.INFO;
            case LEVEL_DEBUG:
                return Level.DEBUG;
            case LEVEL_TRACE:
                return Level.TRACE;
            case LEVEL_ALL:
                return Level.ALL;
            default:
                throw new ValueConversionException("No valid log level: " + value);
        }

    }

    @Override
    public Class<Level> valueType() {
        return Level.class;
    }

    @Override
    public String valuePattern() {
        return LEVEL_OFF + "|"
                + LEVEL_ERROR + "|"
                + LEVEL_WARN + "|"
                + LEVEL_INFO + "|"
                + LEVEL_DEBUG + "|"
                + LEVEL_TRACE + "|"
                + LEVEL_ALL;
    }
}
