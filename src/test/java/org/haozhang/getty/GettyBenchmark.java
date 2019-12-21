package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 10, time = 5)
public class GettyBenchmark extends GettyTestSupport {
    @Benchmark
    public long plainGetter_goodIndex(State state, Blackhole blackhole) {
        return run(plainGetter_goodIndex, state, blackhole);
    }

    @Benchmark
    public long plainGetter_badIndex(State state, Blackhole blackhole) {
        return run(plainGetter_badIndex, state, blackhole);
    }

    @Benchmark
    public long lambdaHelper_goodGetter(State state, Blackhole blackhole) {
        return run(lambdaHelper_goodGetter, state, blackhole);
    }

    @Benchmark
    public long lambdaHelper_badGetter(State state, Blackhole blackhole) {
        return run(lambdaHelper_badGetter, state, blackhole);
    }

    @Benchmark
    public long gettyCached_goodGetter(State state, Blackhole blackhole) {
        return run(gettyCached_goodGetter, state, blackhole);
    }

    @Benchmark
    public long gettyCached_badGetter(State state, Blackhole blackhole) {
        return run(gettyCached_badGetter, state, blackhole);
    }

    @Benchmark
    public long gettyUncached_goodGetter(State state, Blackhole blackhole) {
        return run(gettyUncached_goodGetter, state, blackhole);
    }

    @Benchmark
    public long gettyUncached_badGetter(State state, Blackhole blackhole) {
        return run(gettyUncached_badGetter, state, blackhole);
    }

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
