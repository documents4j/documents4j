package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;

import java.io.File;
import java.io.IOException;

public interface IConverterTestDelegate {

    IConverter getConverter();

    File validDocx() throws IOException;

    File makePdfTarget() throws IOException;

    File corruptDocx() throws IOException;

    File inexistentDocx() throws IOException;
}
