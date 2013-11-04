package no.kantega.pdf.api;

import java.io.File;
import java.io.OutputStream;

/**
 * A conversion with a specified source for the conversion.
 */
public interface IConversionJobWithSourceSpecified {

    /**
     * Configures the current conversion to write the result to the specified file target.
     *
     * @param target The file to which the result of the conversion will be written. Existing files will
     *               be overwritten. If the file is locked by the JVM or any other application or is not writable,
     *               the conversion will abort with an error.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified to(File target);

    /**
     * Configures the current conversion to write the result to the specified file target. Addtionally,
     * a callback is registered.
     *
     * @param target   The file to which the result of the conversion will be written. Existing files will
     *                 be overwritten. If the file is locked by the JVM or any other application or is not writable,
     *                 the conversion will abort with an error.
     * @param callback A callback that is invoked when the conversion terminates.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified to(File target, IFileConsumer callback);

    /**
     * Configures the current conversion to write the result to the specified {@link OutputStream}. The stream
     * will be closed after the conversion is written.
     *
     * @param target The output stream to which the conversion result is written to.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified to(OutputStream target);

    /**
     * Configures the current conversion to write the result to the specified {@link OutputStream}.
     *
     * @param target      The output stream to which the conversion result is written to.
     * @param closeStream Determines whether the output stream is closed after writting the result.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified to(OutputStream target, boolean closeStream);

    /**
     * Configures the current conversion to write the result to invoke the given callback when the conversion
     * terminates.
     *
     * @param callback A callback that is invoked when the conversion terminates.
     * @return The current conversion specification.
     */
    IConversionJobWithPriorityUnspecified to(IInputStreamConsumer callback);
}
