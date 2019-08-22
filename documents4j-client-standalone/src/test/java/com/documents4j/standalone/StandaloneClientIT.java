package com.documents4j.standalone;

import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Ignore("For development/showcase purposes, not meant to be run as unit test.")
public class StandaloneClientIT {

    @Test
    public void main() throws IOException {
        final StandaloneClientOptions options = new StandaloneClientOptions();
        options.baseUri = URI.create("http://localhost:8080");
        IConverter converter = StandaloneClient.asConverter(options);

        final InputStream docxInputStream = this.getClass().getResourceAsStream("/Document.docx");
        final FileOutputStream outputStream = new FileOutputStream("./target/Document.pdf");

        converter.convert(docxInputStream).as(DocumentType.DOCX).to(outputStream).as(DocumentType.PDF).execute();
    }
}
