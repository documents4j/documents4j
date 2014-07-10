package com.documents4j.ws;

import com.documents4j.api.DocumentType;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DocumentTypeAdapter extends XmlAdapter<AdaptedDocumentType, DocumentType> {

    @Override
    public DocumentType unmarshal(AdaptedDocumentType adapted) throws Exception {
        return new DocumentType(adapted.getType(), adapted.getSubtype());
    }

    @Override
    public AdaptedDocumentType marshal(DocumentType original) throws Exception {
        return new AdaptedDocumentType(original);
    }
}
