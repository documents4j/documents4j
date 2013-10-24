package no.kantega.pdf.job;

class StatusCode {

    public static final int OK = 200;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private StatusCode() {
        throw new AssertionError();
    }
}
