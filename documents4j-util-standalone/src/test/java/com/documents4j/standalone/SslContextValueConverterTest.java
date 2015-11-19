package com.documents4j.standalone;

import joptsimple.ValueConversionException;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import static junit.framework.TestCase.assertEquals;

public class SslContextValueConverterTest {

    @Test
    public void testKnownAlgorithm() throws Exception {
        SSLContext sslContext = new SslContextValueConverter().convert("SSL");
        assertEquals("SSL", sslContext.getProtocol());
    }

    @Test(expected = ValueConversionException.class)
    public void testUnknownAlgorithm() throws Exception {
        new SslContextValueConverter().convert("foo");
    }
}
