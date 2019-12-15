package org.haozhang.getty;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GetterTest {
    @Test
    public void typeEquivalence() {
        final Getter<Integer, Double> a = i -> i.doubleValue(); // Lambda method
        final Getter<Integer, Double> b = Integer::doubleValue; // Instance method

        assertThat(a.apply(1), equalTo(1d));
        assertThat(b.apply(1), equalTo(1d));
    }

    @Test
    public void calling() {
        final Map<Integer, Integer> map = Collections.singletonMap(1, 1);
        final Getter<Map<Integer, Integer>, Integer> get1 = m -> m.get(1);

        assertThat(get1.apply(map), equalTo(1));
    }

    @Test
    public void gettyIntegration() {
        final Map<Integer, Integer> map = Collections.singletonMap(1, 1);
        final Getter<Map<Integer, Integer>, Integer> get1 = m -> m.get(1);
        final Getter<Integer, Double> intToDouble = Integer::doubleValue;

        final Double value = Getty.of(map)
            .get(get1)
            .get(intToDouble)
            .get();
        assertThat(value, equalTo(1d));
    }
}
