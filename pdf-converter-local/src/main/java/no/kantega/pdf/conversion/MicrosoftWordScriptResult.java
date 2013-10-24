package no.kantega.pdf.conversion;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.TransformationNativeException;

enum MicrosoftWordScriptResult {

    CONVERSION_SUCCESSFUL(2, null),
    CONVERTER_INTERACTION_SUCCESSFUL(3, null),
    ILLEGAL_INPUT(-2, TransformationNativeException.Reason.ILLEGAL_INPUT),
    TARGET_INACCESSIBLE(-3, TransformationNativeException.Reason.TARGET_INACCESSIBLE),
    INPUT_NOT_FOUND(-4, TransformationNativeException.Reason.INPUT_NOT_FOUND),
    ILLEGAL_CALL(-5, TransformationNativeException.Reason.ILLEGAL_CALL),
    CONVERTER_INACCESSIBLE(-6, TransformationNativeException.Reason.CONVERTER_INACCESSIBLE),
    UNKNOWN(null, TransformationNativeException.Reason.UNKNOWN);

    public static MicrosoftWordScriptResult from(int exitCode) {
        for (MicrosoftWordScriptResult shellResult : MicrosoftWordScriptResult.values()) {
            if (Objects.equal(exitCode, shellResult.getExitCode())) {
                return shellResult;
            }
        }
        return UNKNOWN;
    }

    private final Integer exitCode;
    private final TransformationNativeException.Reason reason;

    private MicrosoftWordScriptResult(Integer exitCode, TransformationNativeException.Reason reason) {
        this.exitCode = exitCode;
        this.reason = reason;
    }

    public TransformationNativeException.Reason toReason() {
        if (reason == null) {
            throw new AssertionError(String.format("%s is not marked as an error state", this));
        } else {
            return reason;
        }
    }

    public MicrosoftWordScriptResult escalateIfNot(MicrosoftWordScriptResult other) {
        if (this != other) {
            throw new TransformationNativeException(toReason());
        }
        return this;
    }

    public Integer getExitCode() {
        return exitCode;
    }
}
