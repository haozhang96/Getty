package org.haozhang.getty;

import org.junit.Before;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.infra.Blackhole;

import java.util.function.Supplier;

public abstract class GettyBenchmarkSupport extends GettyTestSupport {
    // Clear the cache before every benchmark.
    @Before
    public void setup() {
        CACHE.clear();
    }

    // A state holding some random value for each benchmark run to help avoid JIT optimizations
    @org.openjdk.jmh.annotations.State(Scope.Thread)
    public static class State {
        public final long time = System.currentTimeMillis();
    }

    // Enable running benchmarks from the command-line.
    public static final void main(String... args) throws Exception {
        Main.main(args);
    }

    // Benchmark a method while avoiding JIT optimizations.
    protected static final long benchmark(Supplier<?> benchmark, State state, Blackhole blackhole) {
        try {
            blackhole.consume(benchmark.get());
        } catch (Exception exception) {
            // Ignore.
        } finally {
            return state.time;
        }
    }
}
