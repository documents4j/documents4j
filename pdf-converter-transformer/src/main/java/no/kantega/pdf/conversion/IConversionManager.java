package no.kantega.pdf.conversion;

import no.kantega.pdf.api.DocumentType;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

public interface IConversionManager {

    Future<Boolean> startConversion(File source, DocumentType inputFormat, File target, DocumentType outputFormat);

    Map<DocumentType, Set<DocumentType>> getSupportedConversions();

    boolean isOperational();

    void shutDown();
}
