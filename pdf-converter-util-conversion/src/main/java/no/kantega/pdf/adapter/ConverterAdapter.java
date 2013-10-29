package no.kantega.pdf.adapter;

import no.kantega.pdf.api.IConversionJobWithSourceSpecified;
import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IFileSource;
import no.kantega.pdf.api.IInputStreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkState;

public abstract class ConverterAdapter implements IConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterAdapter.class);

    private static final String NO_EXTENSION = "";
    private static final String TEMP_FILE_PREFIX = "temp";

    private final File tempFileFolder;
    private final AtomicLong uniqueNameMaker;
    private final Thread shutDownHook;

    protected ConverterAdapter(File tempFileFolder) {
        this.tempFileFolder = makeTemporaryFolder(tempFileFolder);
        this.uniqueNameMaker = new AtomicLong(1L);
        this.shutDownHook = new ConverterShutdownHook();
        registerShutdownHook();
    }

    @Override
    public IConversionJobWithSourceSpecified convert(File source) {
        return convert(new FileSourceFromFile(source));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(InputStream source) {
        return convert(source, true);
    }

    @Override
    public IConversionJobWithSourceSpecified convert(InputStream source, boolean close) {
        return convert(new InputStreamSourceFromInputStream(source, close));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IFileSource source) {
        return convert(new InputStreamSourceFromFileSource(source));
    }

    @Override
    public IConversionJobWithSourceSpecified convert(IInputStreamSource source) {
        return convert(new FileSourceFromInputStreamSource(source, makeTemporaryFile()));
    }

    private class ConverterShutdownHook extends Thread {
        public ConverterShutdownHook() {
            super(String.format("Shutdown hook: %s", ConverterAdapter.this.getClass().getName()));
        }

        @Override
        public void run() {
            shutDown();
        }
    }

    protected File makeTemporaryFile() {
        return makeTemporaryFile(NO_EXTENSION);
    }

    public File getTempFileFolder() {
        return tempFileFolder;
    }

    protected File makeTemporaryFile(String suffix) {
        return new File(tempFileFolder, String.format("%s%d%s",
                TEMP_FILE_PREFIX, uniqueNameMaker.getAndIncrement(), suffix));
    }

    protected static ExecutorService makeExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
    }

    private static File makeTemporaryFolder(File baseFolder) {
        File tempFileFolder = new File(baseFolder, UUID.randomUUID().toString());
        checkState(tempFileFolder.mkdir(), String.format("Cannot create folder: %s", tempFileFolder));
        return tempFileFolder;
    }

    @Override
    public void shutDown() {
        deregisterShutdownHook();
        deleteOrLog(tempFileFolder);
    }

    protected void registerShutdownHook() {
        try {
            Runtime.getRuntime().addShutdownHook(shutDownHook);
        } catch (IllegalStateException e) {
            LOGGER.info("Tried to register shut down hook in shut down period", e);
        }
    }

    protected void deregisterShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutDownHook);
        } catch (IllegalStateException e) {
            LOGGER.info("Tried to deregister shut down hook in shut down period", e);
        }
    }

    private static void deleteOrLog(File file) {
        if (!file.delete()) {
            LOGGER.warn("Could not delete temporary folder: {}", file);
        }
    }
}
