package com.documents4j.ws;

import com.documents4j.api.DocumentType;

import jakarta.xml.bind.annotation.XmlElement;

public class AdaptedDocumentType {

    private String type;
    private String subtype;

    public AdaptedDocumentType() {
    }

    public AdaptedDocumentType(DocumentType original) {
        type = original.getType();
        subtype = original.getSubtype();
    }

    @XmlElement(required = true, nillable = false)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(required = true, nillable = false)
    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }
}
