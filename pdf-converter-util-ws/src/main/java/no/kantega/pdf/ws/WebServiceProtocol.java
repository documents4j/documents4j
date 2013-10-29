package no.kantega.pdf.ws;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.util.Reaction;

public final class WebServiceProtocol {

    public static final String RESOURCE_PATH = "/";

    public static final String HEADER_JOB_PRIORITY = "Converter-Job-Priority";

    public static enum Status {

        OK(200, Reaction.with(true)),
        CANCEL(530, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The conversion attempt was cancelled"))),
        TIMEOUT(522, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The conversion attempt timed out"))),
        CONVERTER_ERROR(503, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The converter could not process the request"))),
        INPUT_ERROR(422, Reaction.with(new Reaction.ConversionInputExceptionBuilder("The sent input is invalid"))),
        UNKNOWN(500, Reaction.with(false));

        public static Status from(int statusCode) {
            for (Status status : Status.values()) {
                if (Objects.equal(statusCode, status.getStatusCode())) {
                    return status;
                }
            }
            return UNKNOWN;
        }

        public static Status describe(Exception e) {
            if (e instanceof ConversionInputException) {
                return INPUT_ERROR;
            } else {
                return CONVERTER_ERROR;
            }
        }

        private final Integer statusCode;
        private final Reaction reaction;

        private Status(Integer statusCode, Reaction reaction) {
            this.statusCode = statusCode;
            this.reaction = reaction;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean resolve() {
            return reaction.apply();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(Status.class)
                    .add("statusCode", statusCode)
                    .add("reaction", reaction)
                    .toString();
        }
    }

    private WebServiceProtocol() {
        throw new AssertionError();
    }
}
