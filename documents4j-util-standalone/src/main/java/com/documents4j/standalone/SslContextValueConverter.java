package com.documents4j.standalone;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

public class SslContextValueConverter implements ValueConverter<SSLContext> {

    @Override
    public SSLContext convert(String s) {
        try {
            return SSLContext.getInstance(s);
        } catch (NoSuchAlgorithmException e) {
            throw new ValueConversionException("Unknown algorithm: " + s, e);
        }
    }

    @Override
    public Class<? extends SSLContext> valueType() {
        return SSLContext.class;
    }

    @Override
    public String valuePattern() {
        return "<algorithm name>";
    }
}
