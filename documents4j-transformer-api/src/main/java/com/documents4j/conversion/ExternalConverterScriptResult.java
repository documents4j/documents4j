package com.documents4j.conversion;

import com.documents4j.util.Reaction;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents results of running a script as an external process. It encodes conventional error codes and maps
 * them to appropriate actions to these results.
 */
public enum ExternalConverterScriptResult {

    /**
     * Represents a successful conversion of a file.
     */
    CONVERSION_SUCCESSFUL(2, Reaction.with(true)),

    /**
     * Represents a successful interaction with an external converter.
     */
    CONVERTER_INTERACTION_SUCCESSFUL(3, Reaction.with(true)),

    /**
     * Represents an interaction which was aborted because of the source file was found illegal.
     */
    ILLEGAL_INPUT(-2, Reaction.with(new Reaction.ConversionInputExceptionBuilder("The input file seems to be corrupt"))),

    /**
     * Represents an interaction which was aborted because of the target file was inaccessible.
     */
    TARGET_INACCESSIBLE(-3, Reaction.with(new Reaction.FileSystemInteractionExceptionBuilder("Could not access target file"))),

    /**
     * Represents an interaction which was aborted because of the source file was not found on the file system.
     */
    INPUT_NOT_FOUND(-4, Reaction.with(new Reaction.FileSystemInteractionExceptionBuilder("The input file does not exist or cannot be accessed"))),

    /**
     * Represents an interaction with a converter which was not accepted by a converter for formal reasons.
     */
    ILLEGAL_CALL(-5, Reaction.with(new Reaction.ConverterExceptionBuilder("A converter script seems to be erroneous"))),

    /**
     * Represents an interaction with a converter where the converter did not respond.
     */
    CONVERTER_INACCESSIBLE(-6, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The converter seems to be shut down"))),

    /**
     * Represents an interaction with a converter which cannot be explained.
     */
    UNKNOWN(null, Reaction.with(false));
    private final Integer exitValue;
    private final Reaction reaction;

    private ExternalConverterScriptResult(Integer exitValue, Reaction reaction) {
        this.exitValue = exitValue;
        this.reaction = reaction;
    }

    public static ExternalConverterScriptResult from(int exitValue) {
        for (ExternalConverterScriptResult shellResult : ExternalConverterScriptResult.values()) {
            if (Objects.equal(exitValue, shellResult.getExitValue())) {
                return shellResult;
            }
        }
        return UNKNOWN;
    }

    public boolean resolve() {
        return reaction.apply();
    }

    public Integer getExitValue() {
        return exitValue;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ExternalConverterScriptResult.class)
                .add("exitValue", exitValue)
                .add("reaction", reaction)
                .toString();
    }
}
