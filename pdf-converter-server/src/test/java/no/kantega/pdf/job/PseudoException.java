package no.kantega.pdf.job;

import no.kantega.pdf.throwables.ConverterException;

public class PseudoException extends ConverterException {

    public PseudoException() {
        super("This error is emulated");
    }
}
