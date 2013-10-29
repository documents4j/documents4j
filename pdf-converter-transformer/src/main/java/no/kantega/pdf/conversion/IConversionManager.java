package no.kantega.pdf.conversion;

import java.io.File;
import java.util.concurrent.Future;

public interface IConversionManager {

    void shutDown();

    Future<Boolean> startConversion(File source, File target);
}
