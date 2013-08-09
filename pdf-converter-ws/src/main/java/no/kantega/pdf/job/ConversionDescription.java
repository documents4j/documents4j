package no.kantega.pdf.job;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.UUID;

@XmlRootElement(name = "conversion")
public class ConversionDescription {

    public static ConversionDescription from(UUID id, long timeout) {
        ConversionDescription conversionDescription = new ConversionDescription();
        conversionDescription.setId(id);
        conversionDescription.setTimeout(timeout);
        return conversionDescription;
    }

    private UUID id;

    private long timeout;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
