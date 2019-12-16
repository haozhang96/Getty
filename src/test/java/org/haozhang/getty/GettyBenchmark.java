package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

import java.util.Collections;
import java.util.Map;

@Fork(3)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
public class GettyBenchmark {
    @State(Scope.Benchmark)
    public static class GettyState {
        public final Map<Integer, Integer> map = Collections.singletonMap(1, 1);
    }

    @Benchmark
    public void plainGetter(GettyState state) {
        try {
            state.map.get(1).doubleValue();
        } catch (NullPointerException exception) {

        }
    }

    // @Benchmark
    @Fork(3)
    @Warmup(iterations = 2, time = 5)
    @Measurement(iterations = 2, time = 5)
    public void getty_uncached(GettyState state) {
        Getty.of(state.map)
            .get(m -> m.get(1))
            .get(Integer::doubleValue)
            .get();
    }

    @Benchmark
    public void getty_cached(GettyState state) {
        Getty.of(state.map)
            .get(m -> m.get(1))
            .get(Integer::doubleValue)
            .getAndCache();
    }

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
