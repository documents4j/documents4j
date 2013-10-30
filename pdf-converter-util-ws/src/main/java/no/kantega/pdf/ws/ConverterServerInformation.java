package no.kantega.pdf.ws;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = ConverterServerInformation.ROOT_ELEMENT_NAME)
public class ConverterServerInformation {

    public static final String ROOT_ELEMENT_NAME = "remote-converter";

    private boolean operational;
    private long timeout;
    private int protocolVersion;

    public ConverterServerInformation() {
        /* JAX-RS requires default constructor */
    }

    public ConverterServerInformation(boolean operational, long timeout, int protocolVersion) {
        this.operational = operational;
        this.timeout = timeout;
        this.protocolVersion = protocolVersion;
    }

    @XmlElement(required = true, nillable = false)
    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    @XmlElement(required = true, nillable = false)
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
