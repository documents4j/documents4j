package com.documents4j.job;

import com.documents4j.api.IConversionJobWithSourceUnspecified;
import com.documents4j.api.IConverter;
import com.documents4j.api.IFileSource;
import com.documents4j.api.IInputStreamSource;
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

    public static final boolean DEFAULT_CLOSE_STREAM = true;

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

    protected static ExecutorService makeExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>());
    }

    private static File makeTemporaryFolder(File baseFolder) {
        File tempFileFolder = new File(baseFolder, UUID.randomUUID().toString());
        checkState(tempFileFolder.mkdir(), String.format("Cannot create folder: %s", tempFileFolder));
        return tempFileFolder;
    }

    private static void deleteOrLog(File file) {
        if (!file.delete()) {
            LOGGER.warn("Could not delete temporary folder: {}", file);
        }
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(File source, File script) {
        return convert(new FileSourceFromFile(source), new FileSourceFromFile(script));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, InputStream script) {
        return convert(source, script, DEFAULT_CLOSE_STREAM);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, InputStream script, boolean close) {
    	// TODO close applies to both inputstreams.  Control independently?
        return convert(new InputStreamSourceFromInputStream(source, close), 
        		new InputStreamSourceFromInputStream(script, close));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source, IFileSource script) {
        return convert(new InputStreamSourceFromFileSource(source), new InputStreamSourceFromFileSource(script));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source, IInputStreamSource script) {
        return convert(new FileSourceFromInputStreamSource(source, makeTemporaryFile()),
        		new FileSourceFromInputStreamSource(script, makeTemporaryFile()));
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(File source) {
        return convert(new FileSourceFromFile(source), null);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source) {
        return convert(source, null, DEFAULT_CLOSE_STREAM);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(InputStream source, boolean close) {
        return convert(new InputStreamSourceFromInputStream(source, close), 
        		null);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IFileSource source) {
        return convert(new InputStreamSourceFromFileSource(source), null);
    }

    @Override
    public IConversionJobWithSourceUnspecified convert(IInputStreamSource source) {
        return convert(new FileSourceFromInputStreamSource(source, makeTemporaryFile()),
        		null);
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

    @Override
    public void shutDown() {
        cleanUp();
    }

    @Override
    public void kill() {
        cleanUp();
    }

    private void cleanUp() {
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

    private class ConverterShutdownHook extends Thread {
        public ConverterShutdownHook() {
            super(String.format("Shutdown hook: %s", ConverterAdapter.this.getClass().getName()));
        }

        @Override
        public void run() {
            shutDown();
        }
    }

}
