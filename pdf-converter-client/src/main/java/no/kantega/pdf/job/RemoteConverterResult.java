package no.kantega.pdf.job;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.TransformationNativeException;

import javax.ws.rs.core.Response;

enum RemoteConverterResult {

    OK(Response.Status.OK, null),
    SERVICE_UNAVAILABLE(Response.Status.SERVICE_UNAVAILABLE, TransformationNativeException.Reason.OTHER),
    INTERNAL_SERVER_ERROR(Response.Status.INTERNAL_SERVER_ERROR, TransformationNativeException.Reason.OTHER),
    UNKNOWN(null, TransformationNativeException.Reason.OTHER);

    public static RemoteConverterResult from(int statusCode) {
        for (RemoteConverterResult remoteResult : RemoteConverterResult.values()) {
            if (Objects.equal(statusCode, remoteResult.getStatus().getStatusCode())) {
                return remoteResult;
            }
        }
        return UNKNOWN;
    }

    private final Response.Status status;
    private final TransformationNativeException.Reason reason;

    private RemoteConverterResult(Response.Status status, TransformationNativeException.Reason reason) {
        this.status = status;
        this.reason = reason;
    }

    public TransformationNativeException.Reason toReason() {
        if (reason == null) {
            throw new AssertionError(String.format("%s is not marked as an error state", this));
        } else {
            return reason;
        }
    }

    public RemoteConverterResult escalateIfNot(RemoteConverterResult other) {
        if (this != other) {
            throw new TransformationNativeException(toReason());
        }
        return this;
    }

    Response.Status getStatus() {
        return status;
    }
}
