package no.kantega.pdf.ws;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.util.Reaction;

/**
 * This class is a non-instantiable carrier for values that are used in network communication between a
 * conversion server and a remote converter.
 */
public final class ConverterNetworkProtocol {

    /**
     * The current protocol version.
     */
    public static final int CURRENT_PROTOCOL_VERSION = 1;

    /**
     * The root resource path.
     */
    public static final String RESOURCE_PATH = "/";

    /**
     * A header for indicating a job's priority. (optional)
     */
    public static final String HEADER_JOB_PRIORITY = "Converter-Job-Priority";

    /**
     * GZip compression type names.
     */
    public static final String COMPRESSION_TYPE_GZIP = "gzip", COMPRESSION_TYPE_XGZIP = "x-gzip";

    /**
     * A collection of known status codes used for communication.
     */
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

    private ConverterNetworkProtocol() {
        throw new AssertionError();
    }
}
