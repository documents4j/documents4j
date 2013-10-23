package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

public class PseudoConverter extends ConverterAdapter {

    private static final String TEMP_FILE_NAME_PREFIX = "temp";

    private final File folder;
    private final AtomicInteger nameGenerator;

    public PseudoConverter() {
        this.folder = Files.createTempDir();
        this.nameGenerator = new AtomicInteger(0);
    }

    private class PseudoConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IInputStreamSource source;

        private PseudoConversionJobWithSourceSpecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithPriorityUnspecified to(IInputStreamConsumer callback) {
            return new PseudoConversionJobWithPriorityUnspecified(source, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return PseudoConverter.this.makeTemporaryFile(suffix);
        }
    }

    private class PseudoConversionJobWithPriorityUnspecified extends PseudoConversionJob implements IConversionJobWithPriorityUnspecified {

        private PseudoConversionJobWithPriorityUnspecified(IInputStreamSource source, IInputStreamConsumer callback) {
            super(source, callback, IConverter.JOB_PRIORITY_NORMAL);
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new PseudoConversionJob(source, callback, priority);
        }
    }

    private class PseudoConversionJob extends ConversionJobAdapter {

        protected final IInputStreamSource source;
        protected final IInputStreamConsumer callback;
        protected final int priority;

        private PseudoConversionJob(IInputStreamSource source, IInputStreamConsumer callback, int priority) {
            this.source = source;
            this.callback = callback;
            this.priority = priority;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Future<Boolean> schedule() {
            InputStream inputStream = source.getInputStream();
            try {
                ConversionStrategy.from(inputStream).handle(callback);
            } finally {
                source.onConsumed(inputStream);
            }
            return mock(Future.class);
        }
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IInputStreamSource source) {
        return new PseudoConversionJobWithSourceSpecified(source);
    }

    @Override
    protected File makeTemporaryFile(String suffix) {
        return new File(folder, String.format("%s%d%s", TEMP_FILE_NAME_PREFIX, nameGenerator.incrementAndGet(), suffix));
    }

    @Override
    public void shutDown() {
        folder.delete();
    }
}
