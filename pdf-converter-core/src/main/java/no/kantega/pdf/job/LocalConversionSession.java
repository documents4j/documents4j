package no.kantega.pdf.job;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import no.kantega.pdf.util.ConversionException;
import no.kantega.pdf.util.FileTransformationFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalConversionSession implements IConversionSession {

    public static final int JOB_PRIORITY_LOW = 250;
    public static final int JOB_PRIORITY_NORMAL = JOB_PRIORITY_LOW * 2;
    public static final int JOB_PRIORITY_HIGH = JOB_PRIORITY_LOW * 3;

    public static final String SOURCE_FILE_FOLDER_NAME = "source";
    public static final String TARGET_FILE_FOLDER_NAME = "target";

    private static final String DEFAULT_STREAMED_SOURCE_FILE_NAME_PREFIX = "streamed";
    private static final String PDF_FILE_EXTENSION = ".pdf";

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalConversionSession.class);

    private class Conversion implements Callable<FileTransformationFuture<Boolean>> {

        private final File source, target;
        private final int priority;

        private Conversion(File source, File target, int priority) {
            this.source = source;
            this.target = target;
            this.priority = priority;
        }

        @Override
        public FileTransformationFuture<Boolean> call() throws Exception {
            return sessionFactory.newJob(source, target, priority);
        }
    }

    private final String id;
    private final long created;
    private final File sessionFolder, sourceFolder, targetFolder;

    private final AtomicInteger nameCount;

    private final LocalSessionFactory sessionFactory;

    private final Cache<File, FileTransformationFuture<Boolean>> sessionJobs;

    LocalConversionSession(String id, File sessionFolder, LocalSessionFactory sessionFactory) {
        this.id = id;
        this.sessionFolder = sessionFolder;
        sessionFolder.mkdirs();
        this.sourceFolder = makeSubFolder(SOURCE_FILE_FOLDER_NAME);
        this.targetFolder = makeSubFolder(TARGET_FILE_FOLDER_NAME);
        created = System.currentTimeMillis();
        this.sessionFactory = sessionFactory;
        nameCount = new AtomicInteger();
        sessionJobs = CacheBuilder.newBuilder().build();
    }

    private File makeSubFolder(String name) {
        File folder = new File(sessionFolder, name);
        folder.mkdir();
        return folder;
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(File source) {
        return schedule(source, nameTarget(source));
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(File source, File target) {
        return schedule(source, target, JOB_PRIORITY_NORMAL);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(File source, int priority) {
        return schedule(source, nameTarget(source), priority);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(File source, File target, int priority) {
        return makeJob(source, target, priority);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source) {
        return schedule(source, nextName());
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, File target) {
        return schedule(source, nextName(), target);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, int priority) {
        return schedule(source, nextName(), priority);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, File target, int priority) {
        return schedule(source, nextName(), target, priority);
    }

    private String nextName() {
        return String.format("%s-%d", DEFAULT_STREAMED_SOURCE_FILE_NAME_PREFIX, nameCount.getAndIncrement());
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, String name) {
        return schedule(saveToDisk(source, name));
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, String name, File target) {
        return schedule(saveToDisk(source, name), target);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, String name, int priority) {
        return schedule(saveToDisk(source, name), priority);
    }

    @Override
    public FileTransformationFuture<Boolean> schedule(InputStream source, String name, File target, int priority) {
        return schedule(saveToDisk(source, name), target, priority);
    }

    private File saveToDisk(InputStream source, String name) {
        File sourceFile = new File(sourceFolder, name);
        try {
            if (!sourceFile.createNewFile()) {
                throw new ConversionException(String.format("Named stream '%s' is already subject to conversion", name));
            }
            FileOutputStream sourceFileStream = new FileOutputStream(sourceFile);
            try {
                FileLock lock = sourceFileStream.getChannel().lock();
                try {
                    ByteStreams.copy(source, sourceFileStream);
                } finally {
                    lock.release();
                }
            } catch (IOException e) {
                String message = String.format("Could not write stream to %s", sourceFile.getAbsolutePath());
                LOGGER.error(message, e);
                throw new ConversionException(message, e);
            } finally {
                Closeables.close(source, true);
                Closeables.close(sourceFileStream, true);
            }
        } catch (IOException e) {
            String message = String.format("Could not write stream to %s", sourceFile.getAbsolutePath());
            LOGGER.error(message, e);
            throw new ConversionException(message, e);
        }
        return sourceFile;
    }

    private File nameTarget(File source) {
        return new File(targetFolder, source.getName().concat(PDF_FILE_EXTENSION));
    }

    private FileTransformationFuture<Boolean> makeJob(File source, File target, int priority) {
        try {
            return sessionJobs.get(source, new Conversion(source, target, priority));
        } catch (ExecutionException e) {
            String message = String.format("Could not create future for conversion of '%s' to '%s' (priotiy = %d)",
                    source.getAbsolutePath(), target.getAbsolutePath(), priority);
            LOGGER.error(message, e);
            throw new ConversionException(message, e);
        }
    }

    @Override
    public FileTransformationFuture<Boolean> getJobBySource(File source) {
        return sessionJobs.getIfPresent(source);
    }

    @Override
    public FileTransformationFuture<Boolean> getJobByStreamName(String name) {
        return getJobBySource(new File(sourceFolder, Files.getNameWithoutExtension(name)));
    }

    @Override
    public FileTransformationFuture<Boolean> getJobByTarget(File target) {
        for (FileTransformationFuture<Boolean> future : sessionJobs.asMap().values()) {
            if (target.equals(future.getTarget())) {
                return future;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return sessionJobs.asMap().size();
    }

    @Override
    public Set<File> getScheduledFiles() {
        return Collections.unmodifiableSet(sessionJobs.asMap().keySet());
    }

    @Override
    public boolean isComplete() {
        for (Future<Boolean> future : sessionJobs.asMap().values()) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<File> getCurrentlyConvertedFiles() {
        List<File> fileList = new LinkedList<File>();
        for (FileTransformationFuture<?> future : sessionJobs.asMap().values()) {
            if (future.isDone()) {
                fileList.add(future.getTarget());
            }
        }
        return fileList;
    }

    @Override
    public List<File> getConvertedFilesBlocking() {
        List<File> files = new LinkedList<File>();
        for (FileTransformationFuture<Boolean> future : sessionJobs.asMap().values()) {
            try {
                if (future.get()) {
                    files.add(future.getTarget());
                } else {
                    throw new RuntimeException();
                }
            } catch (InterruptedException e) {
                String message = "Interruption while receiving files";
                LOGGER.error(message, e);
                throw new ConversionException(message, e);
            } catch (ExecutionException e) {
                String message = "Execution error while receiving files";
                LOGGER.error(message, e);
                throw new ConversionException(message, e);
            }
        }
        return files;
    }

    @Override
    public LocalConversionSession invalidate() {
        sessionFactory.invalidate(this);
        for (Future<?> future : sessionJobs.asMap().values()) {
            future.cancel(true);
        }
        removeSessionFolder();
        return this;
    }

    @Override
    public boolean isValid() {
        return sessionFactory.findSession(id) != null;
    }

    private void removeSessionFolder() {
        try {
            Runtime.getRuntime().exec(String.format("cmd /c rmdir \"%s\" /s /q", sessionFolder.getAbsolutePath()), null, sessionFolder);
        } catch (IOException e) {
            LOGGER.warn(String.format("Could not remove folder to session %s: %s", id, sessionFolder.getAbsolutePath()), e);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getTimeout() {
        return created + sessionFactory.getSessionIdleTime();
    }
}
