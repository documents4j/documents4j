package no.kantega.pdf.job;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import no.kantega.pdf.conversion.ConversionManager;
import no.kantega.pdf.util.ConversionException;
import no.kantega.pdf.util.IStreamConsumer;

import java.io.*;
import java.nio.channels.FileLock;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LocalConverter implements IConverter {

    public static final class Builder {

        private File baseFolder;
        private int converterCorePoolSize = 10, converterMaximumPoolSize = 15;
        private long fallbackThreadLifeTime = TimeUnit.MINUTES.toMillis(10);
        private long processTimeout = TimeUnit.MINUTES.toMillis(2);

        public Builder baseFolder(File baseFolder) {
            this.baseFolder = baseFolder;
            return this;
        }

        public Builder converterPoolSize(int constant, int fallback, long fallbackThreadLifeTime, TimeUnit timeUnit) {
            this.converterCorePoolSize = constant;
            this.converterMaximumPoolSize = constant + fallback;
            this.fallbackThreadLifeTime = timeUnit.toMillis(fallbackThreadLifeTime);
            return this;
        }

        public Builder processTimeout(long processTimeout, TimeUnit timeUnit) {
            this.processTimeout = timeUnit.toMillis(processTimeout);
            return this;
        }

        public LocalConverter build() {
            return new LocalConverter(baseFolder == null ? Files.createTempDir() : baseFolder,
                    converterCorePoolSize, converterMaximumPoolSize,
                    fallbackThreadLifeTime, TimeUnit.MILLISECONDS,
                    processTimeout, TimeUnit.MILLISECONDS);
        }

    }

    private static final String TEMP_FILE_PREFIX = "stream";

    private final Thread shutdownHook;

    private final File tempFileFolder;
    private final AtomicLong uniqueNameMaker;

    private final ConversionManager conversionManager;
    private final ExecutorService conversionExecutorService;

    protected LocalConverter(File baseFolder, int converterCorePoolSize, int converterMaximumPoolSize,
                             long converterThreadLifeTime, TimeUnit converterThreadLifeTimeUnit,
                             long processTimeout, TimeUnit processTimeoutUnit) {

        tempFileFolder = new File(baseFolder, UUID.randomUUID().toString());
        tempFileFolder.mkdir();
        uniqueNameMaker = new AtomicLong();
        conversionManager = new ConversionManager(baseFolder, processTimeout, processTimeoutUnit);
        conversionExecutorService = new ThreadPoolExecutor(converterCorePoolSize, converterMaximumPoolSize,
                converterThreadLifeTime, converterThreadLifeTimeUnit, new PriorityBlockingQueue<Runnable>());
        Runtime.getRuntime().addShutdownHook(shutdownHook = new LocalConverterShutdownHook());
    }

    @Override
    public Future<Boolean> schedule(File source, IStreamConsumer target) {
        return schedule(source, randomFile(), JOB_PRIORITY_NORMAL, false, true, target);
    }

    @Override
    public Future<Boolean> schedule(File source, File target) {
        return schedule(source, target, JOB_PRIORITY_NORMAL, false, false);
    }

    @Override
    public Future<Boolean> schedule(File source, IStreamConsumer target, int priority) {
        return schedule(source, randomFile(), priority, true, false, target);
    }

    @Override
    public Future<Boolean> schedule(File source, File target, int priority) {
        return schedule(source, target, priority, false, false);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, IStreamConsumer target) {
        return schedule(store(source), randomFile(), JOB_PRIORITY_NORMAL, true, true, target);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target) {
        return schedule(store(source), target, JOB_PRIORITY_NORMAL, true, false);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, IStreamConsumer target, int priority) {
        return schedule(store(source), randomFile(), priority, true, true, target);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target, int priority) {
        return schedule(store(source), target, priority, true, false);
    }

    private Future<Boolean> schedule(File source, File target, int priority, boolean deleteSource, boolean deleteTarget) {
        RunnableFuture<Boolean> job = new WrappingConversionFuture(source, target, priority, deleteSource, deleteTarget, conversionManager);
        conversionExecutorService.submit(job);
        return job;
    }

    private Future<Boolean> schedule(File source, File target, int priority, boolean deleteSource, boolean deleteTarget, IStreamConsumer outputStreamConsumer) {
        RunnableFuture<Boolean> job = new OutputStreamConsumerWrappingConversionFuture(source, target, priority, deleteSource, deleteTarget, conversionManager, outputStreamConsumer);
        conversionExecutorService.submit(job);
        return job;
    }

    @Override
    public boolean convert(File source, IStreamConsumer target) {
        try {
            return schedule(source, target).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert '%s' to cosumer %s)", source.getAbsolutePath(), target), e);
        }
    }

    @Override
    public boolean convert(File source, File target) {
        try {
            return schedule(source, target).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, IStreamConsumer target, int priority) {
        try {
            return schedule(source, target, priority).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert '%s' to consumer %s", source.getAbsolutePath(), target), e);
        }
    }

    @Override
    public boolean convert(File source, File target, int priority) {
        try {
            return schedule(source, target, priority).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, IStreamConsumer target) {
        try {
            return schedule(source, target).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to consumer %s", target), e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target) {
        try {
            return schedule(source, target).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, IStreamConsumer target, int priority) {
        try {
            return schedule(source, target, priority).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to consumer %s", target), e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target, int priority) {
        try {
            return schedule(source, target, priority).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target), e);
        }
    }

    private File randomFile() {
        // Always add '.pdf' file extension to avoid renaming of result since '.pdf' extension is enforced by Visual Basic.
        return new File(tempFileFolder, String.format("%s%d.pdf", TEMP_FILE_PREFIX, uniqueNameMaker.getAndIncrement()));
    }

    private File store(InputStream inputStream) {
        File tempFile = randomFile();
        try {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                FileLock fileLock = fileOutputStream.getChannel().lock();
                try {
                    ByteStreams.copy(inputStream, fileOutputStream);
                } finally {
                    fileLock.release();
                    Closeables.close(fileOutputStream, true);
                }
            } catch (FileNotFoundException e) {
                throw new ConversionException(String.format("Could not create file '%s'", tempFile.getAbsolutePath()), e);
            } finally {
                Closeables.close(inputStream, true);
            }
        } catch (IOException e) {
            throw new ConversionException(String.format("Could not write to temporary file %s", tempFile.getAbsolutePath()), e);
        }
        return tempFile;
    }

    public void shutDown() {
        try {
            conversionManager.shutDown();
            conversionExecutorService.shutdownNow();
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
            /* cannot remove shutdown hook when shut down is in progress */
            }
        } finally {
            tempFileFolder.delete();
        }
    }

    private class LocalConverterShutdownHook extends Thread {
        @Override
        public void run() {
            shutDown();
        }
    }
}
