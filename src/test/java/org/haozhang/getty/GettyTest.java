package org.haozhang.getty;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GettyTest extends GettyTestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(GettyTest.class);

    @Test
    public void unhandledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(GOOD_GETTER)
            .get(Integer::doubleValue)
            .get();

        assertThat(value, equalTo((double) GOOD_VALUE));
    }

    @Test
    public void unhandledGetty_case2() {
        final Double value = Getty.of(MAP)
            .get(NULL_GETTER)
            .get(Integer::doubleValue)
            .get();

        assertThat(value, nullValue());
    }

    @Test
    public void unhandledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(GOOD_GETTER)
            .get((Getter<Integer, Double>) BAD_GETTER)
            .get();

        assertThat(value, nullValue());
    }

    @Test
    public void handledGetty_case1() {
        final Double value = Getty.of(MAP)
            .get(GOOD_GETTER, (i, e) -> -GOOD_VALUE)
            .get(Integer::doubleValue)
            .get();

        assertThat(value, equalTo((double) GOOD_VALUE));
    }

    @Test
    public void handledGetty_case2() {
        final Object value = Getty.of(MAP)
            .get(GOOD_GETTER)
            .get((Getter<Integer, String>) BAD_GETTER, (i, e) -> {
                LOGGER.error("Getter failed on: " + i, e);
                return String.valueOf(-GOOD_VALUE);
            })
            .get();

        assertThat(value, equalTo(String.valueOf(-GOOD_VALUE)));
    }

    @Test
    public void handledGetty_case3() {
        final Object value = Getty.of(MAP)
            .get(GOOD_GETTER, (i, e) -> -GOOD_VALUE)
            .get(Integer::doubleValue, (i, e) -> "string!")
            .get();

        assertThat(value, equalTo((double) GOOD_VALUE));
    }

    @Test
    public void getOrDefault_whenGetterReturnsNull_thenReturnDefaultValue() {
        final Integer value = Getty.of(MAP)
            .getOrDefault(NULL_GETTER, DEFAULT_VALUE)
            .get();

        assertThat(value, equalTo(DEFAULT_VALUE));
    }

    @Test
    public void getOrDefault_whenGetterReturnsValue_thenReturnGetterValue() {
        final Integer value = Getty.of(MAP)
            .getOrDefault(GOOD_GETTER, DEFAULT_VALUE)
            .get();

        assertThat(value, equalTo(GOOD_VALUE));
    }

    @Test(expected = NullPointerException.class)
    public void getNonNull_givenNoExceptionHandler_whenGetterReturnsNull_thenThrowNullPointerException() {
        Getty.of(MAP)
            .getNonNull(NULL_GETTER)
            .get();
    }

    @Test
    public void getNonNull_givenExceptionHandler_whenGetterReturnsNull_thenReturnHandledValue() {
        final Integer value = Getty.of(MAP)
            .getNonNull(NULL_GETTER, (i, e) -> {
                LOGGER.error("Getter failed on: " + i, e);
                return GOOD_VALUE;
            })
            .get();

        assertThat(value, equalTo(GOOD_VALUE));
    }

    //==============================================================================================
    // Caching Helper Methods
    //==============================================================================================

    @Test
    public void chainCaching() {
        // Check Getty instance caching.
        final Getty<Map<Integer, Integer>> a = Getty.cached(MAP);
        final Getty<Map<Integer, Integer>> b = Getty.cached(MAP);
        final Getty<Integer> aGetValue = a.get(GOOD_GETTER);
        final Getty<Integer> bGetValue = b.get(GOOD_GETTER);
        final Getty<Integer> aGetNull = a.get(NULL_GETTER);
        final Getty<Integer> bGetNull = b.get(NULL_GETTER);
        assertThat(a, sameInstance(b)); // Should be the same Getty instance
        assertThat(bGetValue, sameInstance(aGetValue)); // So should the Getty instances on their getter chains
        assertThat(bGetValue, not(sameInstance(a.get(i -> null)))); // But obviously not for different getter output values
        assertThat(bGetNull, sameInstance(aGetNull)); // Same for null values on the chain

        // Call the terminal getter but keep the Getty chain cached.
        final Integer aValue = aGetValue.getAndCache();
        final Integer bValue = bGetValue.getAndCache();
        final Integer aNull = aGetNull.getAndCache();
        final Integer bNull = bGetNull.getAndCache();
        assertThat(bValue, equalTo(aValue));
        assertThat(aNull, nullValue());
        assertThat(bNull, nullValue());

        // Make sure the Getty instances are still cached.
        final Getty<Map<Integer, Integer>> c = Getty.cached(MAP);
        final Getty<Integer> cGetValue = c.get(GOOD_GETTER);
        final Getty<Integer> cGetNull = c.get(NULL_GETTER);
        assertThat(c, sameInstance(a));
        assertThat(c, sameInstance(b));
        assertThat(cGetValue, sameInstance(aGetValue));
        assertThat(cGetValue, sameInstance(bGetValue));
        assertThat(cGetNull, sameInstance(aGetNull));
        assertThat(cGetNull, sameInstance(bGetNull));

        // Call the terminal getter but remove the Getty chain cache. This is the usual case for one-time chains.
        final Integer cValue = cGetValue.get();
        final Integer cNull = cGetNull.get();
        assertThat(cValue, equalTo(aValue));
        assertThat(cNull, nullValue());

        // Make sure the Getty instances are no longer cached.
        final Getty<Map<Integer, Integer>> d = Getty.cached(MAP);
        final Getty<Integer> dGetValue = d.get(GOOD_GETTER);
        assertThat(d, not(sameInstance(a)));
        assertThat(dGetValue, not(sameInstance(aGetValue))); // Should be a brand new instance
    }

    //==============================================================================================
    // Simple Getter Methods
    //==============================================================================================

    @Test(expected = NullPointerException.class)
    public void get_whenValueSupplierIsNull_thenThrowNullPointerException() {
        Getty.get((Supplier<?>) null);
    }

    @Test
    public void get_whenValueSupplierThrowsException_thenReturnNull() {
        assertThat(Getty.get(BAD_SUPPLIER), nullValue());
    }

    @Test
    public void get_whenValueSupplierReturnsNull_thenReturnNull() {
        assertThat(Getty.get(NULL_SUPPLIER), nullValue());
    }

    @Test
    public void get_whenValueSupplierReturnsValue_thenReturnValue() {
        assertThat(Getty.get(GOOD_SUPPLIER), equalTo(GOOD_VALUE));
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefault_whenValueSupplierIsNull_thenThrowNullPointerException() {
        Getty.getOrDefault((Supplier<Integer>) null, NULL_VALUE);
    }

    @Test
    public void getOrDefault_whenValueSupplierThrowsException_thenReturnDefaultValue() {
        assertThat(
            Getty.getOrDefault((Supplier<Integer>) BAD_SUPPLIER, DEFAULT_VALUE),
            equalTo(DEFAULT_VALUE)
        );
    }

    @Test
    public void getOrDefault_whenValueSupplierReturnsNull_thenReturnDefaultValue() {
        assertThat(
            Getty.getOrDefault((Supplier<Integer>) NULL_SUPPLIER, DEFAULT_VALUE),
            equalTo(DEFAULT_VALUE)
        );
    }

    @Test
    public void getOrDefault_whenValueSupplierReturnsValue_thenReturnValue() {
        assertThat(
            Getty.getOrDefault((Supplier<Integer>) GOOD_SUPPLIER, DEFAULT_VALUE),
            equalTo(GOOD_VALUE)
        );
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefault_givenNullValueSupplier_whenDefaultValueSupplierIsNull_thenThrowNullPointerException() {
        Getty.getOrDefault((Supplier<Integer>) NULL_SUPPLIER, (Supplier<Integer>) null);
    }

    @Test
    public void getOrDefault_givenNullValueSupplier_whenDefaultValueSupplierThrowsException_thenReturnNull() {
        assertThat(
            Getty.getOrDefault((Supplier<Integer>) NULL_SUPPLIER, (Supplier<Integer>) BAD_SUPPLIER),
            nullValue()
        );
    }

    @Test
    public void getOrDefault_whenValueSupplierReturnsNull_thenReturnDefaultValueSupplierValue() {
        assertThat(
            Getty.getOrDefault((Supplier<Integer>) NULL_SUPPLIER, (Supplier<Integer>) GOOD_SUPPLIER),
            equalTo(GOOD_VALUE)
        );
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    @Test(expected = NullPointerException.class)
    public void of_whenHeadIsNull_thenThrowNullPointerException() {
        Getty.of(null);
    }

    @Test(expected = NullPointerException.class)
    public void uncached_whenHeadIsNull_thenThrowNullPointerException() {
        Getty.uncached(null);
    }

    @Test
    public void uncached_whenHeadIsNotNull_thenReturnUncachedGettyInstance() {
        final Getty<Map<Integer, Integer>> getty = Getty.uncached(MAP);

        assertThat(getty, notNullValue());
        assertThat(Getty.CACHE.keySet(), not(contains(MAP)));
    }

    @Test(expected = NullPointerException.class)
    public void cached_whenHeadIsNull_thenThrowNullPointerException() {
        Getty.cached(null);
    }

    @Test
    public void cached_whenHeadIsNotNull_thenReturnCachedGettyInstance() {
        final Getty<Map<Integer, Integer>> getty = Getty.cached(MAP);

        assertThat(getty, notNullValue());
        assertThat(Getty.CACHE.keySet(), contains(MAP));
    }
}
