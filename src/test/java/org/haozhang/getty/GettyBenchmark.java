package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Collections;
import java.util.Map;

@Fork(2)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
public class GettyBenchmark {
    private static final Map<Integer, Integer> MAP = Collections.singletonMap(1, 1);
    private static final Getter<Map<Integer, Integer>, Double> GETTER = m -> m.get(1).doubleValue();

    // @State(Scope.Benchmark)
    public static class GettyState {

    }

    @Benchmark
    public void plainGetter() {
        try {
            MAP.get(1).doubleValue();
        } catch (NullPointerException exception) {

        }
    }

    @Benchmark
    public void gettyCached_anonymousLambda() {
        Getty.cached(MAP)
            .get(m -> m.get(1).doubleValue())
            .getAndCache();
    }

    @Benchmark
    public void gettyCached_referencedLambda() {
        Getty.cached(MAP)
            .get(GETTER)
            .getAndCache();
    }

    @Benchmark
    public void gettyUncached_anonymousLambda() {
        Getty.uncached(MAP)
            .get(m -> m.get(1).doubleValue())
            .get();
    }

    @Benchmark
    public void gettyUncached_referencedLambda() {
        Getty.uncached(MAP)
            .get(GETTER)
            .get();
    }

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
