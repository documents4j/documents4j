package com.documents4j.conversion;

import com.documents4j.api.DocumentType;

import java.io.File;
import java.util.concurrent.Future;

public interface IExternalConverter {

    Future<Boolean> startConversion(File source, DocumentType sourceFormat, File target, DocumentType targetType);

    boolean isOperational();

    void shutDown();
}
