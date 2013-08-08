package no.kantega.pdf.job;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.io.Files;
import no.kantega.pdf.conversion.ConversionManager;
import no.kantega.pdf.util.FileTransformationFuture;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LocalSessionFactory implements ISessionFactory {

    public static final class Builder {

        private File baseFolder;
        private int converterCorePoolSize = 10, converterMaximumPoolSize = 15;
        private long fallbackThreadLifeTime = TimeUnit.MINUTES.toMillis(10);
        private long sessionIdleTime = TimeUnit.HOURS.toMillis(5);
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

        public Builder sessionIdleTime(long sessionLifeTime, TimeUnit timeUnit) {
            this.sessionIdleTime = timeUnit.toMillis(sessionLifeTime);
            return this;
        }

        public Builder processTimeout(long processTimeout, TimeUnit timeUnit) {
            this.processTimeout = timeUnit.toMillis(processTimeout);
            return this;
        }

        public LocalSessionFactory build() {
            return new LocalSessionFactory(baseFolder == null ? Files.createTempDir() : baseFolder,
                    converterCorePoolSize, converterMaximumPoolSize,
                    fallbackThreadLifeTime, TimeUnit.MILLISECONDS,
                    sessionIdleTime, TimeUnit.MILLISECONDS,
                    processTimeout, TimeUnit.MILLISECONDS);
        }

    }

    private final Thread shutdownHook;

    private final File baseFolder;

    private final ConversionManager conversionManager;
    private final Cache<String, LocalConversionSession> sessions;

    private final ExecutorService conversionExecutorService;

    private final long sessionIdleTime;

    protected LocalSessionFactory(File baseFolder, int converterCorePoolSize, int converterMaximumPoolSize,
                                  long converterThreadLifeTime, TimeUnit converterThreadLifeTimeUnit,
                                  long sessionIdleTime, TimeUnit sessionIdleTimeUnit,
                                  long processTimeout, TimeUnit processTimeoutUnit) {
        this.baseFolder = baseFolder;
        conversionManager = new ConversionManager(baseFolder, processTimeout, processTimeoutUnit);
        sessions = CacheBuilder.newBuilder().expireAfterAccess(sessionIdleTime, sessionIdleTimeUnit).removalListener(
                new RemovalListener<String, LocalConversionSession>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, LocalConversionSession> notification) {
                        notification.getValue().invalidate();
                    }
                }
        ).build();
        conversionExecutorService = new ThreadPoolExecutor(converterCorePoolSize, converterMaximumPoolSize,
                converterThreadLifeTime, converterThreadLifeTimeUnit, new PriorityBlockingQueue<Runnable>());
        this.sessionIdleTime = sessionIdleTimeUnit.toMillis(sessionIdleTime);
        Runtime.getRuntime().addShutdownHook(shutdownHook = new SessionFactoryShutdown());
    }

    @Override
    public long getSessionIdleTime() {
        return sessionIdleTime;
    }

    @Override
    public LocalConversionSession createSession() {
        String sessionId = UUID.randomUUID().toString();
        LocalConversionSession session = new LocalConversionSession(sessionId, new File(baseFolder, sessionId), this);
        sessions.put(sessionId, session);
        return session;
    }

    @Override
    public LocalConversionSession findSession(String sessionId) {
        return sessions.getIfPresent(sessionId);
    }

    ConversionManager getConversionManager() {
        return conversionManager;
    }

    FileTransformationFuture<Boolean> newJob(File source, File target, int priority) {
        WrappingConversionFuture job = new WrappingConversionFuture(source, target, priority, this);
        conversionExecutorService.submit(job);
        return job;
    }

    void invalidate(LocalConversionSession session) {
        sessions.invalidate(session.getId());
    }

    @Override
    public void shutDown() {
        sessions.invalidateAll();
        conversionManager.shutDown();
        conversionExecutorService.shutdownNow();
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException e) {
            /* cannot remove shutdown hook when shut down is in progress */
        }
    }

    private class SessionFactoryShutdown extends Thread {
        @Override
        public void run() {
            shutDown();
        }
    }
}
