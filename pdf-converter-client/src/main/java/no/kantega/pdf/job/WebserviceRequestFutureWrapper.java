package no.kantega.pdf.job;

import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class WebserviceRequestFutureWrapper implements Future<Boolean> {

    private static final int STATUS_CODE_OK = Response.Status.OK.getStatusCode();

    private final Future<Response> jerseyResponse;

    public WebserviceRequestFutureWrapper(Future<Response> jerseyResponse) {
        this.jerseyResponse = jerseyResponse;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return jerseyResponse.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return jerseyResponse.isCancelled();
    }

    @Override
    public boolean isDone() {
        return jerseyResponse.isDone();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return jerseyResponse.get().getStatus() == STATUS_CODE_OK;
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return jerseyResponse.get(timeout, unit).getStatus() == STATUS_CODE_OK;
    }
}
