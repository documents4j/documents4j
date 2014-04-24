package no.kantega.pdf.ws.standalone;

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
        if (value.equals(LEVEL_OFF)) {
            return Level.OFF;
        } else if (value.equals(LEVEL_ERROR)) {
            return Level.ERROR;
        } else if (value.equals(LEVEL_WARN)) {
            return Level.WARN;
        } else if (value.equals(LEVEL_INFO)) {
            return Level.INFO;
        } else if (value.equals(LEVEL_DEBUG)) {
            return Level.DEBUG;
        } else if (value.equals(LEVEL_TRACE)) {
            return Level.TRACE;
        } else if (value.equals(LEVEL_ALL)) {
            return Level.ALL;
        } else {
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
