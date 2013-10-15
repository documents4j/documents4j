package no.kantega.pdf.throwables;

public class IllegalSourceBatchException extends ConversionBatchException {

    public IllegalSourceBatchException(int statusCode) {
        super(statusCode, "Could not read input. Wrong format? Corrupted?");
    }

}
