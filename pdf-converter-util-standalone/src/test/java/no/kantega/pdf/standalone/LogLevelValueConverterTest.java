package no.kantega.pdf.standalone;

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

    private final String name;
    private final Level level;

    public LogLevelValueConverterTest(String name, Level level) {
        this.name = name;
        this.level = level;
    }

    private ValueConverter<Level> valueConverter;

    @Before
    public void setUp() throws Exception {
        valueConverter = new LogLevelValueConverter();
    }

    @Test
    public void testConversion() throws Exception {
        assertEquals(level, valueConverter.convert(name));
    }
}
