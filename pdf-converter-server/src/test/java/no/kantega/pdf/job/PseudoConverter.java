package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PseudoConverter extends ConverterAdapter {

    private final boolean operational;

    private final DocumentType legalSourceFormat, legalTargetFormat;

    public PseudoConverter(boolean operational, DocumentType legalSourceFormat, DocumentType legalTargetFormat) {
        super(Files.createTempDir());
        this.operational = operational;
        this.legalSourceFormat = legalSourceFormat;
        this.legalTargetFormat = legalTargetFormat;
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return new PseudoConversionJobWithSourceUnspecified(source);
    }

    @Override
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return Collections.singletonMap(legalSourceFormat, Collections.singleton(legalTargetFormat));
    }

    @Override
    public boolean isOperational() {
        return operational;
    }

    @Override
    public void shutDown() {
        super.shutDown();
        assertTrue(getTempFileFolder().getParentFile().delete());
    }

    @Override
    protected void registerShutdownHook() {
        /* do nothing */
    }

    @Override
    protected void deregisterShutdownHook() {
        /* do nothing */
    }

    private class PseudoConversionJobWithSourceUnspecified implements IConversionJobWithSourceUnspecified {

        private final IInputStreamSource source;

        public PseudoConversionJobWithSourceUnspecified(IInputStreamSource source) {
            this.source = source;
        }

        @Override
        public IConversionJobWithSourceSpecified as(DocumentType sourceFormat) {
            return new PseudoConversionJobWithSourceSpecified(source, sourceFormat);
        }
    }

    private class PseudoConversionJobWithSourceSpecified extends ConversionJobWithSourceSpecifiedAdapter {

        private final IInputStreamSource source;
        private final DocumentType sourceFormat;

        private PseudoConversionJobWithSourceSpecified(IInputStreamSource source, DocumentType sourceFormat) {
            this.source = source;
            this.sourceFormat = sourceFormat;
        }

        @Override
        public IConversionJobWithTargetUnspecified to(IInputStreamConsumer callback) {
            return new PseudoConversionJobWithTargetUnspecified(source, sourceFormat, callback);
        }

        @Override
        protected File makeTemporaryFile(String suffix) {
            return PseudoConverter.this.makeTemporaryFile(suffix);
        }
    }

    private class PseudoConversionJobWithTargetUnspecified implements IConversionJobWithTargetUnspecified {

        private final IInputStreamSource source;
        private final DocumentType sourceFormat;

        private final IInputStreamConsumer callback;

        public PseudoConversionJobWithTargetUnspecified(IInputStreamSource source, DocumentType sourceFormat, IInputStreamConsumer callback) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
        }

        @Override
        public IConversionJobWithPriorityUnspecified as(DocumentType targetFormat) {
            return new PseudoConversionJob(source, sourceFormat, callback, targetFormat, IConverter.JOB_PRIORITY_NORMAL);
        }
    }

    private class PseudoConversionJob extends ConversionJobAdapter implements IConversionJobWithPriorityUnspecified {

        protected final IInputStreamSource source;
        protected final IInputStreamConsumer callback;
        protected final int priority;
        private final DocumentType sourceFormat;
        private final DocumentType targetFormat;

        private PseudoConversionJob(IInputStreamSource source, DocumentType sourceFormat, IInputStreamConsumer callback, DocumentType targetFormat, int priority) {
            this.source = source;
            this.sourceFormat = sourceFormat;
            this.callback = callback;
            this.targetFormat = targetFormat;
            this.priority = priority;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Future<Boolean> schedule() {
            InputStream inputStream = source.getInputStream();
            try {
                IStrategyCallback strategyCallback = new InputStreamConsumerStrategyCallbackAdapter(callback);
                if (!legalSourceFormat.equals(sourceFormat) || !legalTargetFormat.equals(targetFormat)) {
                    MockConversion.from(inputStream).overrideWith(MockConversion.FORMAT_ERROR).applyTo(strategyCallback);
                } else if (operational) {
                    MockConversion.from(inputStream).applyTo(strategyCallback);
                } else {
                    MockConversion.from(inputStream).overrideWith(MockConversion.CONVERTER_ERROR).applyTo(strategyCallback);
                }
            } finally {
                source.onConsumed(inputStream);
            }
            return mock(Future.class);
        }

        @Override
        public IConversionJob prioritizeWith(int priority) {
            return new PseudoConversionJob(source, sourceFormat, callback, targetFormat, priority);
        }
    }
}
