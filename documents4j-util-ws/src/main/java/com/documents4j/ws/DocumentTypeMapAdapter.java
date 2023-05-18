package com.documents4j.ws;

import com.documents4j.api.DocumentType;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DocumentTypeMapAdapter extends XmlAdapter<DocumentTypeMapAdapter.Element[], Map<DocumentType, Set<DocumentType>>> {

    public Map<DocumentType, Set<DocumentType>> unmarshal(Element[] adapted) throws Exception {
        Map<DocumentType, Set<DocumentType>> original = new HashMap<DocumentType, Set<DocumentType>>();
        for (Element element : adapted) {
            original.put(element.getKey(), element.getValue());
        }
        return original;
    }

    public Element[] marshal(Map<DocumentType, Set<DocumentType>> original) throws Exception {
        Element[] elements = new Element[original.size()];
        int index = 0;
        for (Map.Entry<DocumentType, Set<DocumentType>> entry : original.entrySet()) {
            elements[index++] = new Element(entry.getKey(), entry.getValue());
        }
        return elements;
    }

    public static class Element {

        private DocumentType key;

        private Set<DocumentType> value;

        private Element() {
        }

        public Element(DocumentType key, Set<DocumentType> value) {
            this.key = key;
            this.value = value;
        }

        @XmlJavaTypeAdapter(DocumentTypeAdapter.class)
        @XmlElement(required = true, nillable = false)
        public DocumentType getKey() {
            return key;
        }

        public void setKey(DocumentType key) {
            this.key = key;
        }

        @XmlJavaTypeAdapter(DocumentTypeAdapter.class)
        @XmlElement(required = true, nillable = false)
        public Set<DocumentType> getValue() {
            return value;
        }

        public void setValue(Set<DocumentType> value) {
            this.value = value;
        }
    }
}
