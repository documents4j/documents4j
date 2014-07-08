package no.kantega.pdf.conversion;

import no.kantega.pdf.api.DocumentType;

import java.io.File;
import java.util.concurrent.Future;

public interface IExternalConverter {

    Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetType);

    boolean isOperational();

    void shutDown();
}
