package com.documents4j.job;

import com.documents4j.api.IInputStreamConsumer;
import com.documents4j.throwables.*;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public enum MockConversion {

    OK(10),
    CANCEL(20),
    CONVERTER_ERROR(30),
    INPUT_ERROR(40),
    FORMAT_ERROR(50),
    FILE_SYSTEM_ERROR(60),
    GENERIC_ERROR(70),
    TIMEOUT(80);

    // Two one byte delimiters plus two byte status code.
    private static final char DELIMITER = '@';

    private final int messageCode;

    private MockConversion(int messageCode) {
        this.messageCode = messageCode;
    }

    public static RichMessage from(InputStream inputStream) {
        String message;
        int messageCode;
        try {
            String raw = new String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8);
            // Get message code.
            assertEquals(DELIMITER, raw.charAt(0));
            int secondDelimiterIndex = raw.indexOf(DELIMITER, 1);
            assertTrue(secondDelimiterIndex >= 0);
            messageCode = Integer.valueOf(raw.substring(1, secondDelimiterIndex));
            message = raw.substring(secondDelimiterIndex + 1);
        } catch (IOException e) {
            throw new AssertionError("Could not read input stream");
        }
        for (MockConversion mockConversion : MockConversion.values()) {
            if (messageCode == mockConversion.getMessageCode()) {
                return mockConversion.new RichMessage(message);
            }
        }
        throw new AssertionError(String.format("%s does not define a value with id %d", MockConversion.class.getSimpleName(), messageCode));
    }

    private int getMessageCode() {
        return messageCode;
    }

    public Future<Boolean> handle(String message, IInputStreamConsumer callback) {
        try {
            switch (this) {
                case OK:
                    callback.onComplete(new ByteArrayInputStream(asReply(message).getBytes(Charsets.UTF_8)));
                    return MockResult.indicating(true);
                case CANCEL:
                    callback.onCancel();
                    return MockResult.forCancellation();
                case CONVERTER_ERROR: {
                    Exception exception = new ConverterAccessException(asReply(message));
                    callback.onException(exception);
                    return MockResult.indicating(exception);
                }
                case INPUT_ERROR: {
                    Exception exception = new ConversionInputException(asReply(message));
                    callback.onException(exception);
                    return MockResult.indicating(exception);
                }
                case FORMAT_ERROR: {
                    Exception exception = new ConversionFormatException(asReply(message));
                    callback.onException(exception);
                    return MockResult.indicating(exception);
                }
                case FILE_SYSTEM_ERROR: {
                    Exception exception = new FileSystemInteractionException(asReply(message));
                    callback.onException(exception);
                    return MockResult.indicating(exception);
                }
                case GENERIC_ERROR: {
                    Exception exception = new ConverterException(asReply(message));
                    callback.onException(exception);
                    return MockResult.indicating(exception);
                }
                case TIMEOUT:
                    // Emulate timeout: Do not invoke callback.
                    return MockResult.forTimeout();
                default:
                    throw new AssertionError(String.format("Unexpected conversion result: %s", this));
            }
        } catch (Exception e) {
            throw new AssertionError(String.format("Unexpected exception: %s", e.getMessage()));
        }
    }

    public InputStream toInputStream(String message) {
        return new ByteArrayInputStream(
                String.format("%c%d%c%s", DELIMITER, this.getMessageCode(), DELIMITER, message)
                        .getBytes(Charsets.UTF_8));

    }

    public File asFile(String message, File file) throws IOException {
        InputStream inputStream = toInputStream(message);
        try {
            Files.asByteSink(file).writeFrom(inputStream);
        } finally {
            inputStream.close();
        }
        return file;
    }

    public String asReply(String message) {
        return String.format("Handled: %s (code: %d)", message, messageCode);
    }

    public class RichMessage {

        private final String message;

        private RichMessage(String message) {
            this.message = message;
        }

        public MockConversion getMockConversion() {
            return MockConversion.this;
        }

        public String getMessage() {
            return message;
        }

        public Future<Boolean> applyTo(IInputStreamConsumer callback) {
            return MockConversion.this.handle(message, callback);
        }

        public RichMessage overrideWith(MockConversion mockConversion) {
            return mockConversion.new RichMessage(message);
        }
    }
}
