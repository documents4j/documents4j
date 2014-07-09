package no.kantega.pdf.conversion.msoffice;

import no.kantega.pdf.api.DocumentType;

public class DocumentTypeProvider {

    private final Document valid, corrupt, inexistent;
    private final DocumentType sourceDocumentType, targetDocumentType;
    private final String targetFileNameSuffix;

    public DocumentTypeProvider(Document valid,
                                Document corrupt,
                                Document inexistent,
                                DocumentType sourceDocumentType,
                                DocumentType targetDocumentType,
                                String targetFileNameSuffix) {
        this.valid = valid;
        this.corrupt = corrupt;
        this.inexistent = inexistent;
        this.sourceDocumentType = sourceDocumentType;
        this.targetDocumentType = targetDocumentType;
        this.targetFileNameSuffix = targetFileNameSuffix;
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
