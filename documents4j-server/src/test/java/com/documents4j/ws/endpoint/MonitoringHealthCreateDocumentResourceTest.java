package com.documents4j.ws.endpoint;

import com.documents4j.api.DocumentType;
import com.documents4j.job.MockConversion;
import com.documents4j.ws.application.WebConverterTestConfiguration;
import org.junit.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class MonitoringHealthCreateDocumentResourceTest {

    @Test
    public void testHealthCreateDocument() {
        MonitoringHealthCreateDocumentResource monitoringHealthCreateDocumentResource = spy(new MonitoringHealthCreateDocumentResource());
        doReturn(MockConversion.OK.toInputStream("")).when(monitoringHealthCreateDocumentResource).getTestStream();
        monitoringHealthCreateDocumentResource.webConverterConfiguration = new WebConverterTestConfiguration(true, 2000L, DocumentType.DOCX, DocumentType.PDF);
        Response response = monitoringHealthCreateDocumentResource.serverInformation();
        assertEquals(200, response.getStatus());
    }
}
