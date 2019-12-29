package org.haozhang.getty;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Map;
import java.util.function.Supplier;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
public class GettyBenchmark extends GettyBenchmarkSupport {
    //==============================================================================================
    // Benchmark Implementations
    //==============================================================================================

    // Plain getters
    private static final Supplier<Integer> plainGetter_goodKey =
        () -> MAP.get(GOOD_KEY);
    private static final Supplier<Integer> plainGetter_nullKey =
        () -> MAP.get(NULL_KEY);
    private static final Supplier<Integer> plainGetter_badKey =
        (Supplier<Integer>) BAD_SUPPLIER;

    // Lambda getter
    private static final Supplier<Integer> lambdaHelper_goodGetter =
        () -> GOOD_GETTER.apply(MAP);
    private static final Supplier<Integer> lambdaHelper_nullGetter =
        () -> NULL_GETTER.apply(MAP);
    private static final Supplier<Integer> lambdaHelper_badGetter =
        () -> ((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).apply(MAP);

    // Getty - cached
    private static final Supplier<Integer> gettyCached_goodGetter =
        () -> Getty.cached(MAP).get(GOOD_GETTER).getAndCache();
    private static final Supplier<Integer> gettyCached_nullGetter =
        () -> Getty.cached(MAP).get(NULL_GETTER).getAndCache();
    private static final Supplier<Integer> gettyCached_badGetter =
        () -> Getty.cached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).getAndCache();

    // Getty - uncached
    private static final Supplier<Integer> gettyUncached_goodGetter =
        () -> Getty.uncached(MAP).get(GOOD_GETTER).get();
    private static final Supplier<Integer> gettyUncached_nullGetter =
        () -> Getty.uncached(MAP).get(NULL_GETTER).get();
    private static final Supplier<Integer> gettyUncached_badGetter =
        () -> Getty.uncached(MAP).get((Getter<Map<Integer, Integer>, Integer>) BAD_GETTER).get();

    //==============================================================================================
    // Benchmark Entry Points
    //==============================================================================================

    @Benchmark
    public long plainGetter_goodKey(State state, Blackhole blackhole) {
        return benchmark(plainGetter_goodKey, state, blackhole);
    }

    @Benchmark
    public long plainGetter_nullKey(State state, Blackhole blackhole) {
        return benchmark(plainGetter_nullKey, state, blackhole);
    }

    @Benchmark
    public long plainGetter_badKey(State state, Blackhole blackhole) {
        return benchmark(plainGetter_badKey, state, blackhole);
    }

    @Benchmark
    public long lambdaHelper_goodGetter(State state, Blackhole blackhole) {
        return benchmark(lambdaHelper_goodGetter, state, blackhole);
    }

    @Benchmark
    public long lambdaHelper_nullGetter(State state, Blackhole blackhole) {
        return benchmark(lambdaHelper_nullGetter, state, blackhole);
    }

    @Benchmark
    public long lambdaHelper_badGetter(State state, Blackhole blackhole) {
        return benchmark(lambdaHelper_badGetter, state, blackhole);
    }

    @Benchmark
    public long gettyCached_goodGetter(State state, Blackhole blackhole) {
        return benchmark(gettyCached_goodGetter, state, blackhole);
    }

    @Benchmark
    public long gettyCached_nullGetter(State state, Blackhole blackhole) {
        return benchmark(gettyCached_nullGetter, state, blackhole);
    }

    @Benchmark
    public long gettyCached_badGetter(State state, Blackhole blackhole) {
        return benchmark(gettyCached_badGetter, state, blackhole);
    }

    @Benchmark
    public long gettyUncached_goodGetter(State state, Blackhole blackhole) {
        return benchmark(gettyUncached_goodGetter, state, blackhole);
    }

    @Benchmark
    public long gettyUncached_nullGetter(State state, Blackhole blackhole) {
        return benchmark(gettyUncached_nullGetter, state, blackhole);
    }

    @Benchmark
    public long gettyUncached_badGetter(State state, Blackhole blackhole) {
        return benchmark(gettyUncached_badGetter, state, blackhole);
    }
}
