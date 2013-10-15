package no.kantega.pdf.throwables;

public class SourceNotFoundBatchException extends ConversionBatchException {

    public SourceNotFoundBatchException(int statusCode) {
        super(statusCode, "Could not find source");
    }
}
