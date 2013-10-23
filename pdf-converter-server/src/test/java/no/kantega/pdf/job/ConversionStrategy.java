package no.kantega.pdf.job;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.ws.application.WebConverterTestConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public enum ConversionStrategy {

    SUCCESS(10, "Success"),
    CANCEL(20, "Cancel"),
    ERROR(30, "Error"),
    TIMEOUT(40, "Timeout");

    // Two one byte delimiters plus two byte status code.
    private static final char DELIMITER = '@';
    private static final String REPLY_INFIX = ": ";

    public class RichMessage {

        private final String message;

        private RichMessage(String message) {
            this.message = message;
        }

        public ConversionStrategy getConversionResult() {
            return ConversionStrategy.this;
        }

        public String getMessage() {
            return message;
        }

        public void handle(IInputStreamConsumer target) {
            ConversionStrategy.this.handle(new ByteArrayInputStream(message.getBytes(Charsets.UTF_8)), target);
        }
    }

    public static RichMessage from(InputStream inputStream) {
        String message;
        int messageCode;
        try {
            String raw = new String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8);
            assertEquals(raw.charAt(0), DELIMITER);
            int secondDelimiterIndex = raw.indexOf(DELIMITER, 1);
            assertTrue(secondDelimiterIndex >= 0);
            messageCode = Integer.valueOf(raw.substring(1, secondDelimiterIndex));
            message = raw.substring(secondDelimiterIndex + 1);

        } catch (IOException e) {
            throw new AssertionError("Could not read input stream");
        }
        for (ConversionStrategy conversionResult : ConversionStrategy.values()) {
            if (messageCode == conversionResult.getMessageCode()) {
                return conversionResult.new RichMessage(message);
            }
        }
        throw new AssertionError(String.format("%s does not define a value with id %d", ConversionStrategy.class.getSimpleName(), messageCode));
    }

    private final int messageCode;
    private final String replyPrefix;

    private ConversionStrategy(int messageCode, String replyPrefix) {
        this.messageCode = messageCode;
        this.replyPrefix = replyPrefix;
    }

    private int getMessageCode() {
        return messageCode;
    }

    void handle(InputStream inputStream, IInputStreamConsumer target) {
        try {
            switch (this) {
                case SUCCESS:
                    onSuccess(inputStream, target);
                    break;
                case CANCEL:
                    onCancel(target);
                    break;
                case ERROR:
                    onError(target);
                    break;
                case TIMEOUT:
                    try {
                        Thread.sleep(250L + WebConverterTestConfiguration.DEFAULT_REQUEST_TIME_OUT);
                    } catch (InterruptedException e) {
                        break;
                    }
                    throw new AssertionError("The thread should have timed out");
                default:
                    throw new AssertionError(String.format("Unexpected conversion result: %s", this));
            }
        } catch (IOException e) {
            throw new AssertionError(String.format("Unexpected IOException: %s", e.getMessage()));
        }
    }

    private void onSuccess(InputStream inputStream, IInputStreamConsumer target) throws IOException {
        byte[] input;
        byte[] prefix = replyPrefix.concat(REPLY_INFIX).getBytes(Charsets.UTF_8);
        byte[] suffix = ByteStreams.toByteArray(inputStream);
        input = new byte[prefix.length + suffix.length];
        System.arraycopy(prefix, 0, input, 0, prefix.length);
        System.arraycopy(suffix, 0, input, prefix.length, suffix.length);
        target.onComplete(new ByteArrayInputStream(input));
    }

    private void onCancel(IInputStreamConsumer target) {
        target.onCancel();
    }

    private void onError(IInputStreamConsumer target) {
        target.onException(new PseudoException());
    }

    public InputStream encode(String content) {
        return new ByteArrayInputStream(
                new StringBuilder()
                        .append(DELIMITER)
                        .append(this.getMessageCode())
                        .append(DELIMITER)
                        .append(content)
                        .toString()
                        .getBytes(Charsets.UTF_8));
    }

    public String asReply(String message) {
        return String.format("%s%s%s", replyPrefix, REPLY_INFIX, message);
    }

}
