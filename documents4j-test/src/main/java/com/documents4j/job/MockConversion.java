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

    public void handle(String message, IInputStreamConsumer callback) {
        try {
            switch (this) {
                case OK:
                    callback.onComplete(new ByteArrayInputStream(asReply(message).getBytes(Charsets.UTF_8)));
                    break;
                case CANCEL:
                    callback.onCancel();
                    break;
                case CONVERTER_ERROR:
                    callback.onException(new ConverterAccessException(asReply(message)));
                    break;
                case INPUT_ERROR:
                    callback.onException(new ConversionInputException(asReply(message)));
                    break;
                case FORMAT_ERROR:
                    callback.onException(new ConversionFormatException(asReply(message)));
                    break;
                case FILE_SYSTEM_ERROR:
                    callback.onException(new FileSystemInteractionException(asReply(message)));
                    break;
                case GENERIC_ERROR:
                    callback.onException(new ConverterException(asReply(message)));
                    break;
                case TIMEOUT:
                    // Emulate timeout: Do not invoke callback.
                    break;
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

        public void applyTo(IInputStreamConsumer callback) {
            MockConversion.this.handle(message, callback);
        }

        public RichMessage overrideWith(MockConversion mockConversion) {
            return mockConversion.new RichMessage(message);
        }
    }
}
