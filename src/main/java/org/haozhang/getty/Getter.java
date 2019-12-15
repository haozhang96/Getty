package org.haozhang.getty;

import java.util.function.Function;

@FunctionalInterface
public interface Getter<T, R> extends Function<T, R> {
}
