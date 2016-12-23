package com.documents4j.conversion;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AbstractExternalConverterTest {

    @Test
    public void testQuote() throws Exception {
        assertEquals("\"foo\" \"bar\"", AbstractExternalConverter.quote("foo", "bar"));
    }

    @Test
    public void testQuoteWithSpaces() throws Exception {
        assertEquals("\"foo bar\" \"qux baz\"", AbstractExternalConverter.quote("foo bar", "qux baz"));
    }

    @Test
    public void testQuoteWithEscape() throws Exception {
        assertEquals("\"\"\"foo\"\"\" \"\"\"bar\"\"\"", AbstractExternalConverter.quote("\"foo\"", "\"bar\""));
    }
}
