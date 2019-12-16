package org.haozhang.getty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandledGetty<T> extends Getty<T> {
    protected static final Map<Object, Map<Object, ExceptionHandledGetty<Object>>> CACHE =
        new ConcurrentHashMap<>();

    protected ExceptionHandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return getInstance(rawGet(getter), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return getInstance(rawGetOrDefault(getter, defaultValue), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueSupplier), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueFunction), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return getInstance(rawGetNonNull(getter), root);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<Object, Map<Object, Getty<Object>>> getCache() {
        return (Map) CACHE;
    }

    @SuppressWarnings("unchecked")
    protected static <R> ExceptionHandledGetty<R> getInstance(R object, Object root) {
        return (ExceptionHandledGetty<R>)
            getInstance(object, root, ExceptionHandledGetty::new, CACHE);
    }
}
