package no.kantega.pdf.job;

import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.api.IConverter;

public abstract class AbstractLocalConverterTest extends AbstractWordBasedTest {

    private IConverter converter;

    @Override
    protected void startConverter() {
        converter = LocalConverter.builder().baseFolder(getTemporaryFolder()).build();
    }

    @Override
    protected void shutDownConverter() {
        converter.shutDown();
    }

    protected IConverter getConverter() {
        return converter;
    }
}
