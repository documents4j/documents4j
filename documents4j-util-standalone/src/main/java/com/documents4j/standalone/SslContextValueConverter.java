package com.documents4j.standalone;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class SslContextValueConverter implements ValueConverter<SSLContext> {

    @Override
    public SSLContext convert(String s) {
        KeyManagerFactory keyManagerFactory;
        try {
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot read default algorithm: " + KeyManagerFactory.getDefaultAlgorithm(), e);
        }
        TrustManagerFactory trustManagerFactory;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot read default algorithm: " + TrustManagerFactory.getDefaultAlgorithm(), e);
        }
        try {
            SSLContext sslContext = SSLContext.getInstance(s);
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            throw new ValueConversionException("Unknown algorithm: " + s, e);
        } catch (KeyManagementException e) {
            throw new ValueConversionException("Cannot manage keys", e);
        }
    }

    @Override
    public Class<? extends SSLContext> valueType() {
        return SSLContext.class;
    }

    @Override
    public String valuePattern() {
        return "<encryption>,<key manager>,<trust manager>";
    }
}
