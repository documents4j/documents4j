package no.kantega.pdf.ws;

import no.kantega.pdf.api.DocumentType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;
import java.util.Set;

/**
 * This bean is used for exchanging meta information between a conversion server and a remote converter.
 */
@XmlRootElement(name = ConverterServerInformation.ROOT_ELEMENT_NAME)
public class ConverterServerInformation {

    public static final String ROOT_ELEMENT_NAME = "remote-converter";

    private boolean operational;
    private long timeout;
    private int protocolVersion;
    private Map<DocumentType, Set<DocumentType>> supported;

    public ConverterServerInformation() {
        /* JAX-RS requires default constructor */
    }

    public ConverterServerInformation(boolean operational,
                                      long timeout,
                                      int protocolVersion,
                                      Map<DocumentType, Set<DocumentType>> supported) {
        this.operational = operational;
        this.timeout = timeout;
        this.protocolVersion = protocolVersion;
        this.supported = supported;
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

    @XmlElement(required = true, nillable = false)
    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @XmlJavaTypeAdapter(DocumentTypeMapAdapter.class)
    @XmlElement(required = true, nillable = false)
    public Map<DocumentType, Set<DocumentType>> getSupported() {
        return supported;
    }

    public void setSupported(Map<DocumentType, Set<DocumentType>> supported) {
        this.supported = supported;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ConverterServerInformation that = (ConverterServerInformation) other;
        return operational == that.operational && protocolVersion == that.protocolVersion
                && timeout == that.timeout && supported.equals(that.supported);
    }

    @Override
    public int hashCode() {
        int result = (operational ? 1 : 0);
        result = 31 * result + (int) (timeout ^ (timeout >>> 32));
        result = 31 * result + protocolVersion;
        result = 31 * result + supported.hashCode();
        return result;
    }
}
