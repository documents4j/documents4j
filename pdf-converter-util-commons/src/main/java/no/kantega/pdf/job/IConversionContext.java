package no.kantega.pdf.job;

import java.util.concurrent.Future;

interface IConversionContext {

    Future<Boolean> asFuture();
}
