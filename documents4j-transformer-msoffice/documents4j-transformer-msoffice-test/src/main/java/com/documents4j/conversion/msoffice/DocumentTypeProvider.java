package com.documents4j.conversion.msoffice;

import com.documents4j.api.DocumentType;

public class DocumentTypeProvider {

    private final Document valid, corrupt, inexistent;
    private final DocumentType sourceDocumentType, targetDocumentType;
    private final String targetFileNameSuffix;
    private final boolean supportsLockedConversion;

    public DocumentTypeProvider(Document valid,
                                Document corrupt,
                                Document inexistent,
                                DocumentType sourceDocumentType,
                                DocumentType targetDocumentType,
                                String targetFileNameSuffix,
                                boolean supportsLockedConversion) {
        this.valid = valid;
        this.corrupt = corrupt;
        this.inexistent = inexistent;
        this.sourceDocumentType = sourceDocumentType;
        this.targetDocumentType = targetDocumentType;
        this.targetFileNameSuffix = targetFileNameSuffix;
        this.supportsLockedConversion = supportsLockedConversion;
    }

    public boolean supportsLockedConversion() {
        return supportsLockedConversion;
    }

    public Document getValid() {
        return valid;
    }

    public Document getCorrupt() {
        return corrupt;
    }

    public Document getInexistent() {
        return inexistent;
    }

    public DocumentType getSourceDocumentType() {
        return sourceDocumentType;
    }

    public DocumentType getTargetDocumentType() {
        return targetDocumentType;
    }

    public String getTargetFileNameSuffix() {
        return targetFileNameSuffix;
    }
}
