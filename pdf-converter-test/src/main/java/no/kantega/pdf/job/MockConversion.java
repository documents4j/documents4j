package no.kantega.pdf.job;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.throwables.ConversionInputException;
import no.kantega.pdf.throwables.ConverterAccessException;
import no.kantega.pdf.throwables.ConverterException;
import no.kantega.pdf.throwables.FileSystemInteractionException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public enum MockConversion {

    OK(10),
    CANCEL(20),
    CONVERTER_ERROR(30),
    INPUT_ERROR(40),
    FILE_SYSTEM_ERROR(50),
    GENERIC_ERROR(60),
    TIMEOUT(70);

    // Two one byte delimiters plus two byte status code.
    private static final char DELIMITER = '@';

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

        public void applyTo(IStrategyCallback callback) {
            MockConversion.this.handle(message, callback);
        }

        public RichMessage overrideWith(MockConversion mockConversion) {
            return mockConversion.new RichMessage(message);
        }
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

    private final int messageCode;

    private MockConversion(int messageCode) {
        this.messageCode = messageCode;
    }

    private int getMessageCode() {
        return messageCode;
    }

    public void handle(String message, IStrategyCallback callback) {
        try {
            switch (this) {
                case OK:
                    onSuccess(message, callback);
                    break;
                case CANCEL:
                    onCancel(callback);
                    break;
                case CONVERTER_ERROR:
                    onError(new ConverterAccessException(asReply(message)), callback);
                    break;
                case INPUT_ERROR:
                    onError(new ConversionInputException(asReply(message)), callback);
                    break;
                case FILE_SYSTEM_ERROR:
                    onError(new FileSystemInteractionException(asReply(message)), callback);
                    break;
                case GENERIC_ERROR:
                    onError(new ConverterException(asReply(message)), callback);
                    break;
                case TIMEOUT:
                    // Emulate timeout: Do nothing.
                    break;
                default:
                    throw new AssertionError(String.format("Unexpected conversion result: %s", this));
            }
        } catch (Exception e) {
            throw new AssertionError(String.format("Unexpected exception: %s", e.getMessage()));
        }
    }

    private void onSuccess(String message, IStrategyCallback callback) throws IOException {
        callback.onComplete(new ByteArrayInputStream(asReply(message).getBytes(Charsets.UTF_8)));
    }

    private void onCancel(IStrategyCallback callback) {
        callback.onCancel();
    }

    private void onError(Exception exception, IStrategyCallback callback) {
        callback.onException(exception);
    }

    public InputStream toInputStream(String message) {
        return new ByteArrayInputStream(
                String.format("%c%d%c%s", DELIMITER, this.getMessageCode(), DELIMITER, message)
                        .getBytes(Charsets.UTF_8));

    }

    public File asFile(String message, File file) throws IOException {
        InputStream inputStream = toInputStream(message);
        try {
            ByteStreams.copy(inputStream, Files.newOutputStreamSupplier(file));
        } finally {
            inputStream.close();
        }
        return file;
    }

    public String asReply(String message) {
        return String.format("Handled: %s (code: %d)", message, messageCode);
    }
}
