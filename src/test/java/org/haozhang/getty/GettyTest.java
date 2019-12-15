package org.haozhang.getty;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GettyTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GettyTest.class);

    private static final Map<Integer, Integer> MAP = new HashMap<>();
    private static final Getter<Map<Integer, Integer>, Integer> GET0 = map -> map.get(0);
    private static final Getter<Map<Integer, Integer>, Integer> GET1 = map -> map.get(1);

    @BeforeClass
    public static void setupClass() {
        for (int i = 0; i < 10; ++i) {
            MAP.put(i, i);
        }
    }

    @Test
    public void chainCaching() {
        // Check Getty instance caching
        final Getty<Map<Integer, Integer>> a = Getty.of(MAP);
        final Getty<Map<Integer, Integer>> b = Getty.of(MAP);
        final Getty<Integer> aGet0 = a.get(GET0);
        final Getty<Integer> bGet0 = b.get(GET0);
        assertThat(a, sameInstance(b)); // Should be the same Getty instance
        assertThat(bGet0, sameInstance(aGet0)); // So should the Getty instances on their getter chains
        assertThat(bGet0, not(sameInstance(a.get(GET1))));

        // Call the final getter but keep the Getty chain cached
        final Integer aValue = aGet0.getAndCache();
        final Integer bValue = bGet0.getAndCache();
        assertThat(bValue, equalTo(aValue));

        // Make sure the Getty instance is still cached
        final Getty<Map<Integer, Integer>> c = Getty.of(MAP);
        final Getty<Integer> cGet0 = c.get(GET0);
        assertThat(c, sameInstance(a));
        assertThat(cGet0, sameInstance(aGet0));

        // Call the final getter but remove the Getty chain cache
        final Integer cValue = cGet0.get();
        assertThat(cValue, equalTo(aValue));

        // Make sure the Getty instance is no longer cached
        final Getty<Map<Integer, Integer>> d = Getty.of(MAP);
        final Getty<Integer> dGet0 = d.get(GET0);
        assertThat(d, not(sameInstance(a)));
        assertThat(dGet0, not(sameInstance(aGet0))); // Not the same as the old instance
        assertThat(dGet0, sameInstance(a.get(GET0))); // But if we call a.get() again, it should return the same instance
    }

    @Test
    public void unhandledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(m -> m.get(0))
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(0d));
    }

    @Test
    public void unhandledGetty_case2() {
        final Double value = Getty.of(MAP)
            .get(m -> m.get(Integer.MAX_VALUE))
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, nullValue());
    }

    @Test
    public void unhandledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(m -> m.get(0))
            .get(i -> { throw new RuntimeException(i.toString()); })
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, nullValue());
    }

    @Test
    public void handledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(m -> m.get(1), (i, e) -> -1)
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(1d));
    }

    @Test
    public void handledGetty_case2() {
        final Object value = Getty.of(MAP)
            .get(m -> m.get(2))
            .get(i -> { throw new RuntimeException(i.toString()); }, (i, e) -> {
                LOGGER.error("Getter failed on: " + i, e);
                return "-2";
            })
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo("-2"));
    }

    @Test
    public void handledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(m -> m.get(3), (i, e) -> -3)
            .get(Integer::doubleValue, (i, e) -> "yikes")
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(3d));
    }
}
