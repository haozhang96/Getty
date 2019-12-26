package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 5, time = 5)
public class GettyBenchmark extends GettyTestSupport {
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

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
