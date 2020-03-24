package com.documents4j.conversion;

import com.documents4j.api.DocumentType;
import com.documents4j.throwables.ConversionInputException;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class ConverterRegistryTest {

    private static final String FOO = "foo/foo", BAR = "bar/bar", QUX = "qux/qux", BAZ = "baz/baz";

    @Test
    public void testViableConversionAnnotation() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(FOO), new DocumentType(BAR)));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(FOO), new DocumentType(BAZ)));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(QUX), new DocumentType(BAR)));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(QUX), new DocumentType(BAZ)));
    }

    @Test(expected = ConversionInputException.class)
    public void testViableConversionsAnnotationThrowsException() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(FOO), new DocumentType(QUX)));
    }

    @Test
    public void testViableConversionsAnnotation() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationsMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(FOO), new DocumentType(BAR)));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(QUX), new DocumentType(BAZ)));
    }

    @Test(expected = ConversionInputException.class)
    public void testViableConversionsAnnotationsThrowsException() throws Exception {
        IExternalConverter externalConverter = new ViableAnnotationsMock();
        ConverterRegistry converterRegistry = new ConverterRegistry(Collections.singleton(externalConverter));
        assertEquals(externalConverter, converterRegistry.lookup(new DocumentType(FOO), new DocumentType(BAZ)));
    }

    @ViableConversion(from = {FOO, QUX}, to = {BAR, BAZ})
    private static class ViableAnnotationMock implements IExternalConverter {

        @Override
        public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat, File script) {
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

    @ViableConversions({@ViableConversion(from = FOO, to = BAR), @ViableConversion(from = QUX, to = BAZ)})
    private static class ViableAnnotationsMock implements IExternalConverter {

        @Override
        public Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat, File script) {
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
}
