package no.kantega.pdf.api;

import java.io.File;
import java.io.InputStream;

/**
 * A converter that allows the conversion of MS Word files to PDF. A converter might have an active life cycle such
 * that its {@link IConverter#shutDown()} method must be called when a converter is not longer used. After a converter
 * is shut down, it cannot be reused.
 */
public interface IConverter {

    /**
     * A conversion of low priority. Recommended for longer lasting batch conversion such that a converter does
     * not block until the batch conversion terminates.
     */
    int JOB_PRIORITY_LOW = 250;

    /**
     * A conversion of normal priority. Recommended for non-urgent conversions that are not requested just-in-time.
     */
    int JOB_PRIORITY_NORMAL = JOB_PRIORITY_LOW * 2;

    /**
     * A conversion of high priority. Recommended for just-in-time conversions.
     */
    int JOB_PRIORITY_HIGH = JOB_PRIORITY_LOW * 3;

    /**
     * Converts a source that is represented as a {@link InputStream}. The input stream will
     * be closed after the conversion is complete.
     *
     * @param source The conversion input as an input stream.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified convert(InputStream source);

    /**
     * Converts a source that is represented as a {@link InputStream}.
     *
     * @param source The conversion input as an input stream.
     * @param close  Whether the {@link InputStream} is closed after the conversion terminates.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified convert(InputStream source, boolean close);

    /**
     * Invokes a callback for the dynamic generation of a input stream source which is additionally
     * informed about the consumption of this source.
     *
     * @param source The input stream source generator.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified convert(IInputStreamSource source);

    /**
     * Converts a source file that is stored on the local file system.
     *
     * @param source The conversion input as a file.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified convert(File source);

    /**
     * Invokes a callback for the dynamic generation of a file source which is additionally informed
     * about the consumption of a source.
     *
     * @param source The file source generator.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceSpecified convert(IFileSource source);

    /**
     * Checks if this converter is currently operational.
     *
     * @return {@code true} if the converter is operational.
     */
    boolean isOperational();

    /**
     * Shuts down this converter. Converters that were shut down can never be used again and must be replaced
     * by a fresh instance.
     */
    void shutDown();
}
