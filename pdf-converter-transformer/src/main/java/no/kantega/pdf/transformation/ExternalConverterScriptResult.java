package no.kantega.pdf.transformation;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterAccessException;
import no.kantega.pdf.throwables.FileSystemInteractionException;

public enum ExternalConverterScriptResult {

    CONVERSION_SUCCESSFUL(2, new IllegalStateException("A successful state is not linked to an exception")),
    CONVERTER_INTERACTION_SUCCESSFUL(3, new IllegalStateException("A successful state is not linked to an exception")),
    ILLEGAL_INPUT(-2, new ConversionInputException("The input file seems to be corrupt")),
    TARGET_INACCESSIBLE(-3, new FileSystemInteractionException("Could not access target file")),
    INPUT_NOT_FOUND(-4, new FileSystemInteractionException("The input file does not exist or cannot be accessed")),
    ILLEGAL_CALL(-5, new ConverterAccessException("A converter script seems to be erroneous")),
    CONVERTER_INACCESSIBLE(-6, new ConverterAccessException("The converter seems to be shut down")),
    UNKNOWN(null, new ConverterAccessException("The converter returned with an unknown exit code"));

    public static ExternalConverterScriptResult from(int exitCode) {
        for (ExternalConverterScriptResult shellResult : ExternalConverterScriptResult.values()) {
            if (Objects.equal(exitCode, shellResult.getExitCode())) {
                return shellResult;
            }
        }
        return UNKNOWN;
    }

    private final Integer exitCode;
    private final RuntimeException exception;

    private ExternalConverterScriptResult(Integer exitCode, RuntimeException exception) {
        this.exitCode = exitCode;
        this.exception = exception;
    }

    public boolean escalateIfNot(ExternalConverterScriptResult other) {
        if (this != other) {
            throw exception;
        }
        return exception instanceof IllegalStateException;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(ExternalConverterScriptResult.class)
                .add("exitCode", exception)
                .add("exception", exception)
                .toString();
    }
}
