package org.haozhang.getty;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandledGetty<T> extends Getty<T> {
    protected static final Map<Object, Set<ExceptionHandledGetty<?>>> CACHE_MAP =
        new ConcurrentHashMap<>();
    protected static final ExceptionHandledGetty<?> NULL = new ExceptionHandledGetty<>(null, null);

    protected ExceptionHandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return handled(rawGet(getter), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return handled(rawGetOrDefault(getter, defaultValue), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return handled(rawGetOrDefault(getter, defaultValueSupplier), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return handled(rawGetOrDefault(getter, defaultValueFunction), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return handled(rawGetNonNull(getter), root);
    }
}
