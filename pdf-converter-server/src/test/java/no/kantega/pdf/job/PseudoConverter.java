package no.kantega.pdf.job;

import com.google.common.io.Files;
import no.kantega.pdf.adapter.ConversionJobAdapter;
import no.kantega.pdf.adapter.ConversionJobWithSourceSpecifiedAdapter;
import no.kantega.pdf.adapter.ConverterAdapter;
import no.kantega.pdf.api.*;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class PseudoConverter extends ConverterAdapter {

    private final boolean operational;

    public PseudoConverter(boolean operational) {
        super(Files.createTempDir());
        this.operational = operational;
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
                IStrategyCallback strategyCallback = new InputStreamConsumerStrategyCallbackAdapter(callback);
                if (operational) {
                    MockConversion.from(inputStream).applyTo(strategyCallback);
                } else {
                    MockConversion.from(inputStream).with(MockConversion.CONVERTER_ERROR).applyTo(strategyCallback);
                }
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
}
