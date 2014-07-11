package com.documents4j.conversion.msoffice;

import java.io.File;

class MicrosoftWordTargetNameCorrector extends MicrosoftOfficeTargetNameCorrector {

    public MicrosoftWordTargetNameCorrector(File target, String fileExtension) {
        super(target, fileExtension);
    }

    @Override
    protected boolean targetHasWrongFileExtension() {
        return false;
    }
}
