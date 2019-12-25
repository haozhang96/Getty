package org.haozhang.getty;

import java.util.function.Function;

/**
 * This functional interface represents a "getter" method that is passed to
 *   {@link Getty#get(Getter)} and its variations.
 * <br/><br/>
 *
 * It is intended to be used as a marker for accepting lambda methods in the form
 *   {@code o -> o.method()}. The Java compiler will match these lambda methods to the correct
 *   interface method ({@link #apply(Object)}).
 * <br/><br/>
 *
 * For example, consider the following Getty chain:
 * <pre>{@code
 *   Getty.of(map).get(m -> m.get(1)).get(Integer::doubleValue).get();
 * }</pre>
 * {@code m -> m.get(1)} becomes a {@code Getter<Map, Integer>} and {@code Integer::doubleValue}
 *   becomes a {@code Getter<Integer, Double>} with the help of the Java compiler.
 *
 * @param <T> The type of object the getter method will be called with; see {@link Getty#object}
 * @param <R> The return type of the getter method
 */
@FunctionalInterface
public interface Getter<T, R> extends Function<T, R> {
}
