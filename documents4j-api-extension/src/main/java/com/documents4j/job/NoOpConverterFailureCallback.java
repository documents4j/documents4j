package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.api.IConverterFailureCallback;
import com.documents4j.throwables.ConverterAccessException;

class NoOpConverterFailureCallback implements IConverterFailureCallback {

    @Override
    public void onFailure(IConverter converter, ConverterAccessException e) {
        /* do nothing */
    }
}
