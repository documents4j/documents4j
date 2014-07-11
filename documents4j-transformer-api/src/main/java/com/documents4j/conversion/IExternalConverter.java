package com.documents4j.conversion;

import com.documents4j.api.DocumentType;

import java.io.File;
import java.util.concurrent.Future;

/**
 * Represents a converter back-end which is capable of applying a document conversion.
 */
public interface IExternalConverter {

    /**
     * Schedules a conversion from an input file to be converted into the target format.
     *
     * @param source       The source file which is to be converted.
     * @param sourceFormat The file format of the source file.
     * @param target       The target file to which the converted file should be saved.
     * @param targetFormat The file format of the target file.
     * @return A future that represents a pending conversion where the enclosed {@code boolean} represents the
     * conversion's success.
     */
    Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat);

    /**
     * Checks if this converter back-end is operational.
     *
     * @return {@code true} if this converter is operational.
     */
    boolean isOperational();

    /**
     * Shuts down this converter.
     */
    void shutDown();
}
