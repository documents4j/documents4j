package no.kantega.pdf.job;

import com.google.common.base.Objects;

import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class WebserviceRequestFutureWrapper implements Future<Boolean> {

    private final Future<Response> futureResponse;

    public WebserviceRequestFutureWrapper(Future<Response> futureResponse) {
        this.futureResponse = futureResponse;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureResponse.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureResponse.isCancelled();
    }

    @Override
    public boolean isDone() {
        return futureResponse.isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return futureResponse.get().getStatus() == RemoteConverterResult.OK.getStatus().getStatusCode();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return futureResponse.get(timeout, unit).getStatus() == RemoteConverterResult.OK.getStatus().getStatusCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(WebserviceRequestFutureWrapper.class)
                .add("futureResponse", futureResponse)
                .toString();
    }
}
