package no.kantega.pdf.job;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConverterAccessException;
import no.kantega.pdf.throwables.ConverterException;

import javax.ws.rs.core.Response;

enum RemoteConverterResult {

    OK(Response.Status.OK, new IllegalStateException("Successful states are not linked to an exception")),
    SERVICE_UNAVAILABLE(Response.Status.SERVICE_UNAVAILABLE, new ConverterAccessException("The remote converter is not available")),
    INTERNAL_SERVER_ERROR(Response.Status.INTERNAL_SERVER_ERROR, new ConverterAccessException("The remote converter caused an error")),
    UNKNOWN(null, new ConverterException("The conversion attempt caused an error"));

    public static RemoteConverterResult from(int statusCode) {
        for (RemoteConverterResult remoteResult : RemoteConverterResult.values()) {
            if (Objects.equal(statusCode, remoteResult.getStatus().getStatusCode())) {
                return remoteResult;
            }
        }
        return UNKNOWN;
    }

    private final Response.Status status;
    private final RuntimeException exception;

    private RemoteConverterResult(Response.Status status, RuntimeException exception) {
        this.status = status;
        this.exception = exception;
    }

    public boolean escalateIfNot(RemoteConverterResult other) {
        if (this != other) {
            throw exception;
        }
        return exception instanceof IllegalStateException;
    }

    Response.Status getStatus() {
        return status;
    }
}
