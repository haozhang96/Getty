package org.haozhang.getty;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GettyTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GettyTest.class);

    // The map used for testing getter-chaining calls
    private static final Map<Integer, Integer> MAP = new HashMap<>();
    static {
        MAP.put(0, 0);
        MAP.put(1, 1);
    }

    // Helpful map-getter lambda methods
    private static final Getter<Map<Integer, Integer>, Integer> GET0 = map -> map.get(0);
    private static final Getter<Map<Integer, Integer>, Integer> GET1 = map -> map.get(1);
    private static final Getter<Map<Integer, Integer>, Integer> GETNULL = map -> map.get(Integer.MAX_VALUE);

    // Commonly-used values
    private static final Integer DEFAULT = 123;

    @Test
    public void chainCaching() {
        // Check Getty instance caching.
        final Getty<Map<Integer, Integer>> a = Getty.of(MAP);
        final Getty<Map<Integer, Integer>> b = Getty.of(MAP);
        final Getty<Integer> aGet0 = a.get(GET0);
        final Getty<Integer> bGet0 = b.get(GET0);
        assertThat(a, sameInstance(b)); // Should be the same Getty instance
        assertThat(bGet0, sameInstance(aGet0)); // So should the Getty instances on their getter chains
        assertThat(bGet0, not(sameInstance(a.get(GET1)))); // But obviously not for different getter output values

        // Call the terminal getter but keep the Getty chain cached.
        final Integer aValue = aGet0.getAndCache();
        final Integer bValue = bGet0.getAndCache();
        assertThat(bValue, equalTo(aValue));

        // Make sure the Getty instances are still cached.
        final Getty<Map<Integer, Integer>> c = Getty.of(MAP);
        final Getty<Integer> cGet0 = c.get(GET0);
        assertThat(c, sameInstance(a));
        assertThat(c, sameInstance(b));
        assertThat(cGet0, sameInstance(aGet0));
        assertThat(cGet0, sameInstance(bGet0));

        // Call the terminal getter but remove the Getty chain cache. This is the usual case for one-time chains.
        final Integer cValue = cGet0.get();
        assertThat(cValue, equalTo(aValue));

        // Make sure the Getty instances are no longer cached.
        final Getty<Map<Integer, Integer>> d = Getty.of(MAP);
        final Getty<Integer> dGet0 = d.get(GET0);
        assertThat(d, not(sameInstance(a)));
        assertThat(dGet0, not(sameInstance(aGet0))); // Should be a brand new instance
        assertThat(dGet0, sameInstance(a.get(GET0))); // But if we call a.get() again, it should return the same instance since d.get() was cached
    }

    @Test
    public void unhandledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(GET0)
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(0d));
    }

    @Test
    public void unhandledGetty_case2() {
        final Double value = Getty.of(MAP)
            .get(GETNULL)
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, nullValue());
    }

    @Test
    public void unhandledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(GET0)
            .get(i -> { throw new RuntimeException("Oops: " + i.toString()); })
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, nullValue());
    }

    @Test
    public void handledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(GET1, (i, e) -> -1)
            .get(Integer::doubleValue)
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(1d));
    }

    @Test
    public void handledGetty_case2() {
        final Object value = Getty.of(MAP)
            .get(GET1)
            .get(i -> { throw new RuntimeException(i.toString()); }, (i, e) -> {
                LOGGER.error("Getter failed on: " + i, e);
                return "-1";
            })
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo("-1"));
    }

    @Test
    public void handledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(GET1, (i, e) -> -1)
            .get(Integer::doubleValue, (i, e) -> "yikes")
            .get();
        LOGGER.info("Value: {}", value);
        assertThat(value, equalTo(1d));
    }

    @Test(expected = NullPointerException.class)
    public void getNonNull_givenNoExceptionHandler_whenGetterReturnsNull_thenThrowNullPointerException() {
        Getty.of(MAP)
            .getNonNull(GETNULL)
            .get();
    }

    @Test
    public void getNonNull_givenExceptionHandler_whenGetterReturnsNull_thenReturnHandledValue() {
        final Integer value = Getty.of(MAP)
            .getNonNull(GETNULL, (i, e) -> {
                LOGGER.error("Getter failed on: " + i, e);
                return DEFAULT;
            })
            .get();
        assertThat(value, equalTo(DEFAULT));
    }

    @Test
    public void getOrDefault_whenGetterReturnsNull_thenReturnDefaultValue() {
        final Integer value = Getty.of(MAP)
            .getOrDefault(GETNULL, DEFAULT)
            .get();
        assertThat(value, equalTo(DEFAULT));
    }

    @Test
    public void getOrDefault_whenGetterReturnsValue_thenReturnGetterValue() {
        final Integer value = Getty.of(MAP)
            .getOrDefault(GET0, DEFAULT)
            .get();
        assertThat(value, equalTo(0));
    }
}
