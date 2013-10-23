package no.kantega.pdf.job;

import javax.ws.rs.core.Response;
import java.util.concurrent.Future;

class RemoteConversionContext implements IConversionContext {

    private final Future<Response> webResponse;

    public RemoteConversionContext(Future<Response> webResponse) {
        this.webResponse = webResponse;
    }

    @Override
    public Future<Boolean> asFuture() {
        return new WebserviceRequestFutureWrapper(webResponse);
    }

    public Future<Response> getWebResponse() {
        return webResponse;
    }
}
