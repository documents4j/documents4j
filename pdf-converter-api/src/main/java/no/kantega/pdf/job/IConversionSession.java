package no.kantega.pdf.job;

import no.kantega.pdf.util.FileTransformationFuture;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IConversionSession {

    FileTransformationFuture<Boolean> schedule(File source);

    FileTransformationFuture<Boolean> schedule(File source, File target);

    FileTransformationFuture<Boolean> schedule(File source, int priority);

    FileTransformationFuture<Boolean> schedule(File source, File target, int priority);

    FileTransformationFuture<Boolean> schedule(InputStream source);

    FileTransformationFuture<Boolean> schedule(InputStream source, File target);

    FileTransformationFuture<Boolean> schedule(InputStream source, int priority);

    FileTransformationFuture<Boolean> schedule(InputStream source, File target, int priority);

    FileTransformationFuture<Boolean> schedule(InputStream source, String name);

    FileTransformationFuture<Boolean> schedule(InputStream source, String name, File target);

    FileTransformationFuture<Boolean> schedule(InputStream source, String name, int priority);

    FileTransformationFuture<Boolean> schedule(InputStream source, String name, File target, int priority);

    FileTransformationFuture<Boolean> getJobBySource(File source);

    FileTransformationFuture<Boolean> getJobByStreamName(String name);

    FileTransformationFuture<Boolean> getJobByTarget(File target);

    Set<File> getScheduledFiles();

    boolean isValid();

    boolean isComplete();

    int size();

    List<File> getCurrentlyConvertedFiles();

    List<File> getConvertedFilesBlocking();

    IConversionSession invalidate();

    String getId();

    long getTimeout();
}
