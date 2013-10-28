package no.kantega.pdf.job;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import no.kantega.pdf.throwables.ConverterException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public enum MockConversion {

    VALID(10),
    CANCEL(20),
    ERROR(30),
    TIMEOUT(40);

    // Two one byte delimiters plus two byte status code.
    private static final char DELIMITER = '@';
    private static final String REPLY_PREFIX = "Handled: ";

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

        public void handle(IStrategyCallback callback) {
            MockConversion.this.handle(message, callback);
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

    void handle(String message, IStrategyCallback callback) {
        try {
            switch (this) {
                case VALID:
                    onSuccess(message, callback);
                    break;
                case CANCEL:
                    onCancel(callback);
                    break;
                case ERROR:
                    onError(message, callback);
                    break;
                case TIMEOUT:
                    // Do nothing.
                    break;
                default:
                    throw new AssertionError(String.format("Unexpected conversion result: %s", this));
            }
        } catch (Exception e) {
            throw new AssertionError(String.format("Unexpected exception: %s", e.getMessage()));
        }
    }

    private void onSuccess(String message, IStrategyCallback callback) throws IOException {
        byte[] input;
        byte[] prefix = REPLY_PREFIX.getBytes(Charsets.UTF_8);
        byte[] suffix = message.getBytes(Charsets.UTF_8);
        input = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, input, 0, prefix.length);
        System.arraycopy(suffix, 0, input, prefix.length, suffix.length);
        callback.onComplete(new ByteArrayInputStream(input));
    }

    private void onCancel(IStrategyCallback callback) {
        callback.onCancel();
    }

    private void onError(String message, IStrategyCallback callback) throws IOException, ClassNotFoundException {
        callback.onException(new ConverterException(asReply(message)));
    }

    public InputStream asInputStream(String message) {
        return new ByteArrayInputStream(
                new StringBuilder()
                        .append(DELIMITER)
                        .append(this.getMessageCode())
                        .append(DELIMITER)
                        .append(message)
                        .toString()
                        .getBytes(Charsets.UTF_8));

    }

    public File asFile(String message, File file) throws IOException {
        InputStream inputStream = asInputStream(message);
        try {
            ByteStreams.copy(inputStream, Files.newOutputStreamSupplier(file));
        } finally {
            inputStream.close();
        }
        return file;
    }

    public static String asReply(String message) {
        return String.format("%s%s", REPLY_PREFIX, message);
    }

}
