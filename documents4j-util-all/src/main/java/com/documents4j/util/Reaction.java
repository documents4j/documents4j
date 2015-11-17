package com.documents4j.util;

import com.documents4j.throwables.*;
import com.google.common.base.MoreObjects;

/**
 * Represents a reaction to an event within the conversion chain.
 */
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

    /**
     * Applies the reaction to a given event.
     *
     * @return The result of the conversion.
     */
    public abstract boolean apply();

    /**
     * A builder for obtaining a runtime exception for a given error event.
     */
    public interface IExceptionBuilder {

        /**
         * Creates a runtime exception for a given error event.
         *
         * @return The exception for the given error event.
         */
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
            return MoreObjects.toStringHelper(BooleanReaction.class)
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
            return MoreObjects.toStringHelper(BooleanReaction.class)
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
            return MoreObjects.toStringHelper(ConverterExceptionBuilder.class)
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
            return MoreObjects.toStringHelper(ConverterAccessExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }

    public static class ConversionFormatExceptionBuilder implements IExceptionBuilder {

        private final String message;

        public ConversionFormatExceptionBuilder(String message) {
            this.message = message;
        }

        @Override
        public RuntimeException make() {
            return new ConversionFormatException(message);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(ConversionFormatExceptionBuilder.class)
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
            return MoreObjects.toStringHelper(ConversionInputExceptionBuilder.class)
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
            return MoreObjects.toStringHelper(ConversionInputExceptionBuilder.class)
                    .add("message", message)
                    .toString();
        }
    }
}
