package no.kantega.pdf.job;

import no.kantega.pdf.AbstractWordBasedTest;
import no.kantega.pdf.api.IConverter;

class LocalConverterTestDelegate extends AbstractWordBasedTest implements IConverterTestDelegate {

    private IConverter converter;

    @Override
    protected void startConverter() {
        converter = LocalConverter.builder().baseFolder(getTemporaryFolder()).build();
    }

    @Override
    protected void shutDownConverter() {
        converter.shutDown();
    }

    @Override
    public IConverter getConverter() {
        return converter;
    }

    @Override
    protected boolean converterRunsOnExit() {
        return true;
    }
}