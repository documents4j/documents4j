package com.documents4j.conversion;

import com.documents4j.util.Reaction;
import com.google.common.base.Objects;

public enum ExternalConverterScriptResult {

    CONVERSION_SUCCESSFUL(2, Reaction.with(true)),
    CONVERTER_INTERACTION_SUCCESSFUL(3, Reaction.with(true)),
    ILLEGAL_INPUT(-2, Reaction.with(new Reaction.ConversionInputExceptionBuilder("The input file seems to be corrupt"))),
    TARGET_INACCESSIBLE(-3, Reaction.with(new Reaction.FileSystemInteractionExceptionBuilder("Could not access target file"))),
    INPUT_NOT_FOUND(-4, Reaction.with(new Reaction.FileSystemInteractionExceptionBuilder("The input file does not exist or cannot be accessed"))),
    ILLEGAL_CALL(-5, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("A converter script seems to be erroneous"))),
    CONVERTER_INACCESSIBLE(-6, Reaction.with(new Reaction.ConverterAccessExceptionBuilder("The converter seems to be shut down"))),
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
        return Objects.toStringHelper(ExternalConverterScriptResult.class)
                .add("exitValue", exitValue)
                .add("reaction", reaction)
                .toString();
    }
}
