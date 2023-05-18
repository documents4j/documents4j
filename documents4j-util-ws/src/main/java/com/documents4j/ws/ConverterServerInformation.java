package com.documents4j.ws;

import com.documents4j.api.DocumentType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
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
    private Map<DocumentType, Set<DocumentType>> supportedConversions;

    public ConverterServerInformation() {
        /* JAX-RS requires default constructor */
    }

    public ConverterServerInformation(boolean operational,
                                      long timeout,
                                      int protocolVersion,
                                      Map<DocumentType, Set<DocumentType>> supportedConversions) {
        this.operational = operational;
        this.timeout = timeout;
        this.protocolVersion = protocolVersion;
        this.supportedConversions = supportedConversions;
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
    public Map<DocumentType, Set<DocumentType>> getSupportedConversions() {
        return supportedConversions;
    }

    public void setSupportedConversions(Map<DocumentType, Set<DocumentType>> supportedConversions) {
        this.supportedConversions = supportedConversions;
    }
}
