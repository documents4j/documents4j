package no.kantega.pdf.ws;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.util.Reaction;

import javax.ws.rs.core.Response;

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

    private static final int RESPONSE_STATUS_CODE_CANCEL = 530;

    private static final int RESPONSE_STATUS_CODE_TIMEOUT = 522;

    private static final int RESPONSE_STATUS_CODE_INPUT_ERROR = 422;

    private ConverterNetworkProtocol() {
        throw new UnsupportedOperationException();
    }

    /**
     * A collection of known status codes used for communication.
     */
    public static enum Status {

        OK(Response.Status.OK.getStatusCode(),
                Reaction.with(true)),
        CANCEL(RESPONSE_STATUS_CODE_CANCEL,
                Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The conversion attempt was cancelled"))),
        TIMEOUT(RESPONSE_STATUS_CODE_TIMEOUT,
                Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The conversion attempt timed out"))),
        CONVERTER_ERROR(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The converter could not process the request"))),
        INPUT_ERROR(RESPONSE_STATUS_CODE_INPUT_ERROR,
                Reaction.with(new Reaction.ConversionInputExceptionBuilder("The sent input is invalid"))),
        UNKNOWN(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                Reaction.with(false));
        private final Integer statusCode;
        private final Reaction reaction;

        private Status(Integer statusCode, Reaction reaction) {
            this.statusCode = statusCode;
            this.reaction = reaction;
        }

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
}
