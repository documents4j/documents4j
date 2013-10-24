package no.kantega.pdf.job;

import no.kantega.pdf.AbstractJerseyTest;
import no.kantega.pdf.api.IConverter;

import java.io.File;
import java.io.IOException;

public class RemoteConverterTestDelegate extends AbstractJerseyTest implements IConverterTestDelegate {

    private IConverter converter;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        converter = RemoteConverter.make(getBaseUri());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            converter.shutDown();
        } finally {
            super.tearDown();
        }
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    public File validDocx() throws IOException {
        return null;
    }

    @Override
    public File makePdfTarget() throws IOException {
        return null;
    }

    @Override
    public File corruptDocx() throws IOException {
        return null;
    }

    @Override
    public File inexistentDocx() throws IOException {
        return null;
    }
}
