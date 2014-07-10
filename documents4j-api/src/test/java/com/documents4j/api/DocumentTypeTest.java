package com.documents4j.api;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DocumentTypeTest {

    private static final String FOO = "foo", BAR = "bar";

    @Test
    public void testDocumentTypeEquality() throws Exception {
        assertEquals(new DocumentType(FOO, BAR).hashCode(), new DocumentType(FOO + "/" + BAR).hashCode());
        assertEquals(new DocumentType(FOO, BAR), new DocumentType(FOO + "/" + BAR));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtypeMissingThrowsException() throws Exception {
        new DocumentType(FOO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubtypeStubThrowsException() throws Exception {
        new DocumentType(FOO + "/");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValueConstructorCannotBeInvoked() throws Exception {
        Constructor<?> constructor = DocumentType.Value.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail();
        } catch (InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        }
    }
}
