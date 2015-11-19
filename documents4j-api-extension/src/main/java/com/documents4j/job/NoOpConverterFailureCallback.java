package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.api.IConverterFailureCallback;

class NoOpConverterFailureCallback implements IConverterFailureCallback {

    @Override
    public void onFailure(IConverter converter) {
        /* do nothing */
    }
}
