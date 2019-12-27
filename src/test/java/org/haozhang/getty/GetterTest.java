package org.haozhang.getty;

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class GetterTest extends GettyTestSupport {
    @Test
    public void typeEquivalence() {
        final Getter<Integer, Double> a = i -> i.doubleValue(); // Lambda method
        final Getter<Integer, Double> b = Integer::doubleValue; // Instance method

        assertThat(a.apply(1), equalTo(1d));
        assertThat(b.apply(1), equalTo(1d));
    }

    @Test
    public void calling() {
        final Getter<Map<Integer, Integer>, Integer> getter = map -> map.get(GOOD_KEY);

        assertThat(getter.apply(MAP), equalTo(GOOD_VALUE));
    }

    @Test
    public void gettyIntegration() {
        final Getter<Map<Integer, Integer>, Integer> getter = map -> map.get(GOOD_KEY);
        final double value = Getty.of(MAP)
            .get(getter)
            .get(Integer::doubleValue)
            .get();

        assertThat(value, equalTo((double) GOOD_VALUE));
    }
}
