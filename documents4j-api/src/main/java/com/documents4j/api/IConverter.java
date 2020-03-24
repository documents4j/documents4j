package com.documents4j.api;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * A converter that allows the conversion of documents. A converter might have an active life cycle such that
 * its {@link IConverter#shutDown()} method must be called when a converter is not longer used. After a converter
 * is shut down, it cannot be reused but needs to be recreated.
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
    IConversionJobWithSourceUnspecified convert(InputStream source);

    /**
     * Converts a source that is represented as a {@link InputStream}.
     *
     * @param source The conversion input as an input stream.
     * @param close  Whether the {@link InputStream} is closed after the conversion terminates.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(InputStream source, boolean close);

    /**
     * Invokes a callback for the dynamic generation of a input stream source which is additionally
     * informed about the consumption of this source.
     *
     * @param source The input stream source generator.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(IInputStreamSource source);

    /**
     * Converts a source file that is stored on the local file system.
     *
     * @param source The conversion input as a file.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(File source);

    /**
     * Invokes a callback for the dynamic generation of a file source which is additionally informed
     * about the consumption of a source.
     *
     * @param source The file source generator.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(IFileSource source);
        
    /**
     * Converts a source that is represented as a {@link InputStream}. The input stream will
     * be closed after the conversion is complete.
     *
     * @param source The conversion input as an input stream.
     * @param script The conversion script as an input stream.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(InputStream source, InputStream script);

    /**
     * Converts a source that is represented as a {@link InputStream}.
     *
     * @param source The conversion input as an input stream.
     * @param script The conversion script as an input stream.
     * @param close  Whether the {@link InputStream} is closed after the conversion terminates.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(InputStream source, InputStream script, boolean close);

    /**
     * Invokes a callback for the dynamic generation of a input stream source which is additionally
     * informed about the consumption of this source.
     *
     * @param source The input stream source generator.
     * @param script The conversion script as an input stream source generator.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(IInputStreamSource source, IInputStreamSource script);

    /**
     * Converts a source file that is stored on the local file system.
     *
     * @param source The conversion input as a file.
     * @param script The conversion script as a file.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(File source, File script);

    /**
     * Invokes a callback for the dynamic generation of a file source which is additionally informed
     * about the consumption of a source.
     *
     * @param source The file source generator.
     * @param script The conversion script as a file source.
     * @return The current conversion specification.
     */
    IConversionJobWithSourceUnspecified convert(IFileSource source, IFileSource script);

    /**
     * Returns a mapping of all conversions that are supported by the backing conversion engine.
     *
     * @return A map of all possible conversions with the key describing the input types and the set
     * describing the formats that these input types can be converted into.
     */
    Map<DocumentType, Set<DocumentType>> getSupportedConversions();

    /**
     * Checks if this converter is currently operational, i.e. it can convert documents and is not shut down.
     *
     * @return {@code true} if the converter is operational.
     */
    boolean isOperational();

    /**
     * Shuts down this converter gracefully. The converter does no longer accept new conversion requests but awaits running conversions to complete
     * for a given period of time.
     */
    void shutDown();

    /**
     * Shuts down this converter immediately. All running conversions are aborted.
     */
    void kill();
}
