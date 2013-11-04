package no.kantega.pdf.job;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.ws.WebServiceProtocol;

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
        return handle(futureResponse.get());
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return handle(futureResponse.get(timeout, unit));
    }

    private boolean handle(Response response) throws ExecutionException {
        try {
            return WebServiceProtocol.Status.from(response.getStatus()).resolve();
        } catch (ConverterException e) {
            throw new ExecutionException("The conversion resulted in an error", e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(WebserviceRequestFutureWrapper.class)
                .add("futureResponse", futureResponse)
                .toString();
    }
}
