package com.documents4j.conversion;

import com.documents4j.api.DocumentType;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * A manager for conversions which offers a common interface for querying different converter back-ends.
 */
public interface IConversionManager {

    /**
     * Schedules a conversion from an input file to be converted into the target format.
     *
     * @param source       The source file which is to be converted.
     * @param sourceFormat The file format of the source file.
     * @param target       The target file to which the converted file should be saved.
     * @param targetFormat The file format of the target file.
     * @param script       A specific script to use for the conversion (can be null).
     * @return A future that represents a pending conversion where the enclosed {@code boolean} represents the
     * conversion's success.
     */
    Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetFormat, File script);

    /**
     * Returns a mapping of all conversions that are supported by the backing conversion engine.
     *
     * @return A map of all possible conversions with the key describing the input types and the set
     * describing the formats that these input types can be converted into.
     */
    Map<DocumentType, Set<DocumentType>> getSupportedConversions();

    /**
     * Checks if <b>all</b> converter back-ends that are represented by this conversion manager are operational.
     *
     * @return {@code true} if the all represented converters are operational.
     */
    boolean isOperational();

    /**
     * Shuts down all converter back-ends that are represented by this conversion manager.
     */
    void shutDown();
}
