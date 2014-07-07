package no.kantega.pdf.conversion;

import no.kantega.pdf.throwables.ConversionInputException;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ConverterRegistryTest {

    private static final String FOO = "foo", BAR = "bar", QUX = "qux", BAZ = "baz";

    @ViableConversion(from = {FOO, QUX}, to = {BAR, BAZ})
    private static class ViableAnnotationMock implements IExternalConverter {

        @Override
        public Future<Boolean> startConversion(File source, File target) {
            throw new AssertionError();
        }

        @Override
        public boolean isOperational() {
            throw new AssertionError();
        }

        @Override
        public void shutDown() {
            throw new AssertionError();
        }
    }

    @Test
    public void testViableConversionAnnotation() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(FOO, BAR));
        assertEquals(externalConverter, converterRegistry.lookup(FOO, BAZ));
        assertEquals(externalConverter, converterRegistry.lookup(QUX, BAR));
        assertEquals(externalConverter, converterRegistry.lookup(QUX, BAZ));
    }

    @Test(expected = ConversionInputException.class)
    public void testViableConversionsAnnotationThrowsException() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        converterRegistry.lookup(FOO, QUX);
    }

    @ViableConversions({@ViableConversion(from = FOO, to = BAR), @ViableConversion(from = QUX, to = BAZ)})
    private static class ViableAnnotationsMock implements IExternalConverter {

        @Override
        public Future<Boolean> startConversion(File source, File target) {
            throw new AssertionError();
        }

        @Override
        public boolean isOperational() {
            throw new AssertionError();
        }

        @Override
        public void shutDown() {
            throw new AssertionError();
        }
    }

    @Test
    public void testViableConversionsAnnotation() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationsMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(FOO, BAR));
        assertEquals(externalConverter, converterRegistry.lookup(QUX, BAZ));
    }

    @Test(expected = ConversionInputException.class)
    public void testViableConversionsAnnotationsThrowsException() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationsMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        converterRegistry.lookup(FOO, BAZ);
    }
}
