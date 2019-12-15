package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;

public class GettyBenchmark {
    @State(Scope.Benchmark)
    public static class GettyState {

    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    @Fork(value = 1, warmups = 2)
    @Warmup(iterations = 3)
    public void a() {
        int i = 1;
    }

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
