package no.kantega.pdf.util;

import com.google.common.base.Objects;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterAccessException;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.throwables.FileSystemInteractionException;

public abstract class Reaction {

    Reaction() {
        /* empty, but suppress visibility outside of package */
    }

    public static Reaction with(boolean value) {
        return new BooleanReaction(value);
    }

    public static Reaction with(IExceptionBuilder builder) {
        return new ExceptionalReaction(builder);
    }

    public abstract boolean apply();

    public static interface IExceptionBuilder {

        RuntimeException make();
    }

    private static class BooleanReaction extends Reaction {

        private final boolean value;

        public BooleanReaction(boolean value) {
            this.value = value;
        }

        @Override
        public boolean apply() {
            return value;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(BooleanReaction.class)
                    .add("value", value)
                    .toString();
        }

    }

    private static class ExceptionalReaction extends Reaction {

        private final IExceptionBuilder builder;

        public ExceptionalReaction(IExceptionBuilder builder) {
            this.builder = builder;
        }

        @Override
        public boolean apply() {
            throw builder.make();
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(BooleanReaction.class)
                    .add("exceptionBuilder", builder)
                    .toString();
        }
    }

    public static class ConverterExceptionBuilder implements IExceptionBuilder {

        private final String message;

        public ConverterExceptionBuilder(String message) {
            this.message = message;
        }

        @Override
        public RuntimeException make() {
            return new ConverterException(message);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(ConverterExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }

    public static class ConverterAccessExceptionBuilder implements IExceptionBuilder {

        private final String message;

        public ConverterAccessExceptionBuilder(String message) {
            this.message = message;
        }

        @Override
        public RuntimeException make() {
            return new ConverterAccessException(message);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(ConverterAccessExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }

    public static class ConversionInputExceptionBuilder implements IExceptionBuilder {

        private final String message;

        public ConversionInputExceptionBuilder(String message) {
            this.message = message;
        }

        @Override
        public RuntimeException make() {
            return new ConversionInputException(message);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(ConversionInputExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }

    public static class FileSystemInteractionExceptionBuilder implements IExceptionBuilder {

        private final String message;

        public FileSystemInteractionExceptionBuilder(String message) {
            this.message = message;
        }

        @Override
        public RuntimeException make() {
            return new FileSystemInteractionException(message);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(ConversionInputExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }
}
