package no.kantega.pdf.standalone;

public class LogDescription {

    public static final String LOG_PATTERN = "%date %-5level [%thread] %logger{42} - %message%n";

    public static final int MAXIMUM_LOG_HISTORY_INDEX = 10;
    public static final String MAXIMUM_LOG_FILE_SIZE = "10MB";

    private LogDescription() {
        throw new UnsupportedOperationException();
    }
}
