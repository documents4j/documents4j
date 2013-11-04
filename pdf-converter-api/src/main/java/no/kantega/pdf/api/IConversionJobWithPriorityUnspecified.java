package no.kantega.pdf.api;

/**
 * A sufficiently specified conversion with default priority that is not yet applied to the converter.
 */
public interface IConversionJobWithPriorityUnspecified extends IConversionJob {

    /**
     * Sets a priority for the conversion that is currently specified.
     *
     * @param priority A priority where a higher priority gives a hint to the converter to prefer this conversion.
     * @return The current conversion specification.
     */
    IConversionJob prioritizeWith(int priority);
}
