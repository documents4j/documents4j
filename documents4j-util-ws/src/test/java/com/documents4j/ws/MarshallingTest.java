package com.documents4j.ws;

import com.documents4j.api.DocumentType;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class MarshallingTest {

    private static final boolean OPERATIONAL = true;
    private static final int PROTOCOL_VERSION = 42;
    private static final long TIMEOUT = 48L;
    private static final DocumentType FIRST_SAMPLE_TYPE = new DocumentType("foo", "bar");
    private static final DocumentType SECOND_SAMPLE_TYPE = new DocumentType("qux", "baz");

    private static ConverterServerInformation makeSample() {
        ConverterServerInformation converterServerInformation = new ConverterServerInformation();
        converterServerInformation.setOperational(OPERATIONAL);
        converterServerInformation.setProtocolVersion(PROTOCOL_VERSION);
        converterServerInformation.setTimeout(TIMEOUT);
        Map<DocumentType, Set<DocumentType>> supported = new HashMap<DocumentType, Set<DocumentType>>();
        Set<DocumentType> first = new HashSet<DocumentType>();
        first.addAll(Arrays.asList(FIRST_SAMPLE_TYPE, SECOND_SAMPLE_TYPE));
        supported.put(FIRST_SAMPLE_TYPE, first);
        Set<DocumentType> second = new HashSet<DocumentType>();
        second.add(FIRST_SAMPLE_TYPE);
        supported.put(SECOND_SAMPLE_TYPE, second);
        converterServerInformation.setSupportedConversions(supported);
        return converterServerInformation;
    }

    @Test
    public void testXmlMarshalling() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(ConverterServerInformation.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
        jaxbMarshaller.marshal(makeSample(), outputStream);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ConverterServerInformation unmarshalled = (ConverterServerInformation) jaxbUnmarshaller.unmarshal(new ByteArrayInputStream(outputStream.toByteArray()));
        ConverterServerInformation original = makeSample();
        assertEquals(original.isOperational(), unmarshalled.isOperational());
        assertEquals(original.getProtocolVersion(), unmarshalled.getProtocolVersion());
        assertEquals(original.getTimeout(), unmarshalled.getTimeout());
        assertEquals(original.getSupportedConversions(), unmarshalled.getSupportedConversions());
    }
}
