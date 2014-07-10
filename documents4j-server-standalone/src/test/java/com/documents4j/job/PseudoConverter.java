package com.documents4j.job;

import com.documents4j.api.DocumentType;
import com.documents4j.conversion.IExternalConverter;
import com.documents4j.conversion.ViableConversion;

import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ViableConversion(from = {}, to = {})
public class PseudoConverter implements IExternalConverter {

    public PseudoConverter(File baseFolder, long processTimeout, TimeUnit timeUnit) {
        /* do nothing */
    }

    @Override
    public Future<Boolean> startConversion(File source,
                                           DocumentType sourceFormat,
                                           File target,
                                           DocumentType targetType) {
        throw new AssertionError();
    }

    @Override
    public boolean isOperational() {
        throw new AssertionError();
    }

    @Override
    public void shutDown() {
        /* do nothing*/
    }
}
