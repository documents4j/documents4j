package com.documents4j.job;

import com.documents4j.api.IConverter;
import com.documents4j.api.ISelectionStrategy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;

public class RoundRobinSelectionStrategyTest {

    private static final int SAMPLE_SIZE = 10000;

    private IConverter first, second, third;

    @Before
    public void setUp() throws Exception {
        first = Mockito.mock(IConverter.class);
        second = Mockito.mock(IConverter.class);
        third = Mockito.mock(IConverter.class);
    }

    @Test
    public void testSimpleSelection() throws Exception {
        ISelectionStrategy selectionStrategy = new RoundRobinSelectionStrategy();
        for (int index = 0; index < SAMPLE_SIZE; index++) {
            IConverter converter = selectionStrategy.select(Arrays.asList(first, second, third));
            switch (index % 3) {
                case 0:
                    assertEquals(first, converter);
                    break;
                case 1:
                    assertEquals(second, converter);
                    break;
                case 2:
                    assertEquals(third, converter);
                    break;
                default:
                    throw new AssertionError();
            }
        }
    }
}
