package org.haozhang.getty;

import org.junit.Before;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class GettyTestSupport {
    // Keep a reference to the same cache as the one in Getty using reflection.
    protected static Map<Object, GettyChain> CACHE = null;
    static { try {
        final Field cacheField = Getty.class.getDeclaredField("CACHE");
        cacheField.setAccessible(true);
        CACHE = (Map<Object, GettyChain>) cacheField.get(null);
    } catch (Exception exception) { } }

    // Keys for the map below
    protected static final int GOOD_KEY = 1;
    protected static final int NULL_KEY = -1;

    // Expected values for the map below
    protected static final Integer GOOD_VALUE = 1;
    protected static final Integer NULL_VALUE = null;
    protected static final Integer DEFAULT_VALUE = 123;

    // The head object to start Getty chains with
    protected static final Map<Integer, Integer> MAP =
        Collections.singletonMap(GOOD_KEY, GOOD_VALUE);

    // Helpful map-getter lambda methods
    protected static final Getter<Map<Integer, Integer>, Integer> GOOD_GETTER =
        map -> map.get(GOOD_KEY);
    protected static final Getter<Map<Integer, Integer>, Integer> NULL_GETTER =
        map -> map.get(NULL_KEY);
    protected static final Getter<?, ?> BAD_GETTER =
        object -> { throw new RuntimeException(); };

    // Helpful suppliers
    protected static final Supplier<?> GOOD_SUPPLIER =
        () -> GOOD_VALUE;
    protected static final Supplier<?> NULL_SUPPLIER =
        () -> null;
    protected static final Supplier<?> BAD_SUPPLIER =
        () -> { throw new RuntimeException(); };

    // A state holding some random value for each benchmark run
    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {
        public final long time = System.currentTimeMillis();
    }

    // Benchmark a method while avoiding JIT optimizations
    protected static final long benchmark(Supplier<?> benchmark, State state, Blackhole blackhole) {
        try {
            blackhole.consume(benchmark.get());
        } catch (Exception exception) {
            // Ignore.
        } finally {
            return state.time;
        }
    }

    // Clear the cache before every benchmark.
    @Before
    public void setup() {
        CACHE.clear();
    }

    /**
     * Benchmarks to be fed to benchmark()
     */

    // Plain getters
    protected static final Supplier<Integer> plainGetter_goodKey =
        () -> MAP.get(GOOD_KEY);
    protected static final Supplier<Integer> plainGetter_nullKey =
        () -> MAP.get(NULL_KEY);
    protected static final Supplier<Integer> plainGetter_badKey =
        (Supplier<Integer>) BAD_SUPPLIER;

    // Lambda getter
    protected static final Supplier<Integer> lambdaHelper_goodGetter =
        () -> GOOD_GETTER.apply(MAP);
    protected static final Supplier<Integer> lambdaHelper_nullGetter =
        () -> NULL_GETTER.apply(MAP);
    protected static final Supplier<Integer> lambdaHelper_badGetter =
        () -> ((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).apply(MAP);

    // Getty - cached
    protected static final Supplier<Integer> gettyCached_goodGetter =
        () -> Getty.cached(MAP).get(GOOD_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyCached_nullGetter =
        () -> Getty.cached(MAP).get(NULL_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyCached_badGetter =
        () -> Getty.cached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).getAndCache();

    // Getty - uncached
    protected static final Supplier<Integer> gettyUncached_goodGetter =
        () -> Getty.uncached(MAP).get(GOOD_GETTER).get();
    protected static final Supplier<Integer> gettyUncached_nullGetter =
        () -> Getty.uncached(MAP).get(NULL_GETTER).get();
    protected static final Supplier<Integer> gettyUncached_badGetter =
        () -> Getty.uncached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).get();
}
