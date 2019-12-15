package org.haozhang.getty;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHandledGetty<T> extends Getty<T> {
    protected static final Map<Object, LinkedHashSet<ExceptionHandledGetty<?>>> CACHE_MAP =
        new ConcurrentHashMap<>();
    protected static final ExceptionHandledGetty<?> NULL =
        new ExceptionHandledGetty<>(null, null);

    protected ExceptionHandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return handled(rawGet(getter), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue
    ) {
        return handled(rawGetOrDefault(getter, defaultValue), root);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return handled(rawGetNonNull(getter), root);
    }
}
