package no.kantega.pdf.throwables;

public class TransformationNativeException extends TransformationException {

    public static enum Reason {
        ILLEGAL_INPUT, TARGET_INACCESSIBLE, INPUT_NOT_FOUND, ILLEGAL_CALL, CONVERTER_INACCESSIBLE, UNKNOWN
    }

    private final Reason reason;

    public TransformationNativeException(Reason reason) {
        super(String.format("Could not convert: %s", reason));
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
