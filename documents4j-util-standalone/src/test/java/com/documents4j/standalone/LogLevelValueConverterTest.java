package com.documents4j.standalone;

import ch.qos.logback.classic.Level;
import joptsimple.ValueConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class LogLevelValueConverterTest {

    private final String name;
    private final Level level;
    private ValueConverter<Level> valueConverter;

    public LogLevelValueConverterTest(String name, Level level) {
        this.name = name;
        this.level = level;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"off", Level.OFF},
                {"error", Level.ERROR},
                {"warn", Level.WARN},
                {"info", Level.INFO},
                {"debug", Level.DEBUG},
                {"trace", Level.TRACE},
                {"all", Level.ALL},
        });
    }

    @Before
    public void setUp() throws Exception {
        valueConverter = new LogLevelValueConverter();
    }

    @Test
    public void testConversion() throws Exception {
        assertEquals(level, valueConverter.convert(name));
    }
}
