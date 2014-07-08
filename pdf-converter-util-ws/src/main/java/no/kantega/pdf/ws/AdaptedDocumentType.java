package no.kantega.pdf.ws;

import no.kantega.pdf.api.DocumentType;

import javax.xml.bind.annotation.XmlElement;

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

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AdaptedDocumentType that = (AdaptedDocumentType) other;
        return subtype.equals(that.subtype) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subtype.hashCode();
        return result;
    }
}
