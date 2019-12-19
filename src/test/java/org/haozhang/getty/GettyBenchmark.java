package org.haozhang.getty;

import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;

@Fork(1)
@Warmup(iterations = 3, time = 5)
@Measurement(iterations = 10, time = 5)
public class GettyBenchmark extends GettyTestSupport {
    @Benchmark
    public void plainGetter_goodIndex() {
        try {
            plainGetter_goodIndex.get().doubleValue();
        } catch (NullPointerException exception) { }
    }

    @Benchmark
    public void plainGetter_badIndex() {
        try {
            plainGetter_badIndex.get().doubleValue();
        } catch (NullPointerException exception) { }
    }

    @Benchmark
    public void lambdaHelper_goodGetter() {
        try {
            lambdaHelper_goodGetter.get();
        } catch (NullPointerException exception) { }
    }

    @Benchmark
    public void lambdaHelper_badGetter() {
        try {
            lambdaHelper_badGetter.get();
        } catch (NullPointerException exception) { }
    }

    @Benchmark
    public void gettyCached_goodGetter() {
        gettyCached_goodGetter.get();
    }

    @Benchmark
    public void gettyCached_badGetter() {
        gettyCached_badGetter.get();
    }

    @Benchmark
    public void gettyUncached_goodGetter() {
        gettyUncached_goodGetter.get();
    }

    @Benchmark
    public void gettyUncached_badGetter() {
        gettyUncached_badGetter.get();
    }

    public static void main(String... args) throws Exception {
        Main.main(args);
    }
}
