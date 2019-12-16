package org.haozhang.getty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected static final Map<Object, Map<Object, ExceptionUnhandledGetty<?>>> CACHE =
        new ConcurrentHashMap<>();

    protected ExceptionUnhandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return unhandled(rawGet(getter), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return unhandled(rawGetOrDefault(getter, defaultValue), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return unhandled(rawGetOrDefault(getter, defaultValueSupplier), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return unhandled(rawGetOrDefault(getter, defaultValueFunction), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getNonNull(Getter<T, R> getter) {
        return unhandled(rawGetNonNull(getter), root);
    }
}
