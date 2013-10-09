package no.kantega.pdf.job;

import no.kantega.pdf.api.IConverter;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IStreamConsumer;
import no.kantega.pdf.defaults.NoopFileConsumer;
import no.kantega.pdf.throwables.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.Future;

public class RemoteConverter implements IConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteConverter.class);

    private final long requestWaitingTolerance;

    private final WebTarget webTarget;

    public RemoteConverter(URI baseUri, long requestWaitingTolerance) {
        this.webTarget = ClientBuilder.newClient().target(baseUri);
        this.requestWaitingTolerance = requestWaitingTolerance;
    }

    @Override
    public Future<Boolean> schedule(File source, IStreamConsumer target) {
        return schedule(read(source), target, JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(File source, File target) {
        return schedule(read(source), new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(File source, File target, IFileConsumer callback) {
        return schedule(read(source), new StreamToFileConsumer(target, callback), JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(File source, IStreamConsumer target, int priority) {
        return schedule(read(source), target, priority, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(File source, File target, int priority) {
        return schedule(read(source), new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), priority, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(File source, File target, IFileConsumer callback, int priority) {
        return schedule(read(source), new StreamToFileConsumer(target, callback), priority, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, IStreamConsumer target) {
        return schedule(source, target, JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target) {
        return schedule(source, new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target, IFileConsumer callback) {
        return schedule(source, new StreamToFileConsumer(target, callback), JOB_PRIORITY_NORMAL, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, IStreamConsumer target, int priority) {
        return schedule(source, target, priority, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target, int priority) {
        return schedule(source, new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), priority, requestWaitingTolerance);
    }

    @Override
    public Future<Boolean> schedule(InputStream source, File target, IFileConsumer callback, int priority) {
        return schedule(source, new StreamToFileConsumer(target, callback), priority, requestWaitingTolerance);
    }

    private Future<Boolean> schedule(InputStream source, IStreamConsumer target, int priority, long requestWaitingTolerance) {
        return null;
    }

    @Override
    public boolean convert(File source, IStreamConsumer target) {
        try {
            return schedule(read(source), target, JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s'", source.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, File target) {
        try {
            return schedule(read(source), new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, File target, IFileConsumer callback) {
        try {
            return schedule(read(source), new StreamToFileConsumer(target, callback), JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, IStreamConsumer target, int priority) {
        try {
            return schedule(read(source), target, priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s'", source.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, File target, int priority) {
        try {
            return schedule(read(source), new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(File source, File target, IFileConsumer callback, int priority) {
        try {
            return schedule(read(source), new StreamToFileConsumer(target, callback), priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert from '%s' to '%s'", source.getAbsolutePath(), target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, IStreamConsumer target) {
        try {
            return schedule(source, target, JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException("Could not convert", e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target) {
        try {
            return schedule(source, new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target, IFileConsumer callback) {
        try {
            return schedule(source, new StreamToFileConsumer(target, callback), JOB_PRIORITY_NORMAL, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, IStreamConsumer target, int priority) {
        try {
            return schedule(source, target, priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException("Could not convert", e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target, int priority) {
        try {
            return schedule(source, new StreamToFileConsumer(target, NoopFileConsumer.INSTANCE), priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target.getAbsolutePath()), e);
        }
    }

    @Override
    public boolean convert(InputStream source, File target, IFileConsumer callback, int priority) {
        try {
            return schedule(source, new StreamToFileConsumer(target, callback), priority, 0L).get();
        } catch (Exception e) {
            throw new ConversionException(String.format("Could not convert to '%s'", target.getAbsolutePath()), e);
        }
    }

    private InputStream read(File file) {
        return new LazyFileInputStream(file);
    }
}
