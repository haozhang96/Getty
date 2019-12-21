package org.haozhang.getty;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class GettyTestSupport {
    // Expected values for the map below
    protected static final int VALUE = 1;
    protected static final int DEFAULT_VALUE = 123;

    // Keys for the map below
    protected static final int GOOD_KEY = 1;
    protected static final int NULL_KEY = -1;

    // The main object to start Getty chains with
    protected static final Map<Integer, Integer> MAP = Collections.singletonMap(GOOD_KEY, VALUE);

    // Helpful map-getter lambda methods
    protected static final Getter<Map<Integer, Integer>, Integer> GOOD_GETTER =
        map -> map.get(GOOD_KEY);
    protected static final Getter<Map<Integer, Integer>, Integer> NULL_GETTER =
        map -> map.get(NULL_KEY);
    protected static final Getter<?, ?> BAD_GETTER =
        __ -> { throw new RuntimeException(); };

    // Benchmarks to be fed to run()
    protected static final Supplier<Integer> plainGetter_goodIndex =
        () -> MAP.get(GOOD_KEY);
    protected static final Supplier<Integer> plainGetter_badIndex =
        () -> MAP.get(NULL_KEY);
    protected static final Supplier<Integer> lambdaHelper_goodGetter =
        () -> GOOD_GETTER.apply(MAP);
    protected static final Supplier<Integer> lambdaHelper_badGetter =
        () -> NULL_GETTER.apply(MAP);
    protected static final Supplier<Integer> gettyCached_goodGetter =
        () -> Getty.cached(MAP).get(GOOD_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyCached_badGetter =
        () -> Getty.cached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyUncached_goodGetter =
        () -> Getty.uncached(MAP).get(GOOD_GETTER).get();
    protected static final Supplier<Integer> gettyUncached_badGetter =
        () -> Getty.uncached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).get();

    // A state holding some random value for each benchmark run
    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {
        public final long time = System.currentTimeMillis();
    }

    // Run a method while avoiding JIT optimizations
    protected static final long run(Supplier<?> method, State state, Blackhole blackhole) {
        try {
            blackhole.consume(method.get());
        } catch (Exception exception) {
            // Ignore.
        } finally {
            return state.time;
        }
    }
}
