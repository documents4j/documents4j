package no.kantega.pdf.job;

import no.kantega.pdf.util.IStreamConsumer;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Future;

public interface IConverter {

    int JOB_PRIORITY_LOW = 250;
    int JOB_PRIORITY_NORMAL = JOB_PRIORITY_LOW * 2;
    int JOB_PRIORITY_HIGH = JOB_PRIORITY_LOW * 3;

    Future<Boolean> schedule(File source, IStreamConsumer target);

    Future<Boolean> schedule(File source, File target);

    Future<Boolean> schedule(File source, IStreamConsumer target, int priority);

    Future<Boolean> schedule(File source, File target, int priority);

    Future<Boolean> schedule(InputStream source, IStreamConsumer target);

    Future<Boolean> schedule(InputStream source, File target);

    Future<Boolean> schedule(InputStream source, IStreamConsumer target, int priority);

    Future<Boolean> schedule(InputStream source, File target, int priority);

    boolean convert(File source, IStreamConsumer target);

    boolean convert(File source, File target);

    boolean convert(File source, IStreamConsumer target, int priority);

    boolean convert(File source, File target, int priority);

    boolean convert(InputStream source, IStreamConsumer target);

    boolean convert(InputStream source, File target);

    boolean convert(InputStream source, IStreamConsumer target, int priority);

    boolean convert(InputStream source, File target, int priority);
}
