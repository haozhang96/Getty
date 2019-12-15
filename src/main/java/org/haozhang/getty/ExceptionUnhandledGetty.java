package org.haozhang.getty;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected static final Map<Object, LinkedHashSet<ExceptionUnhandledGetty<?>>> CACHE_MAP = new ConcurrentHashMap<>();
    protected static final ExceptionUnhandledGetty<?> NULL = new ExceptionUnhandledGetty<>(null, null);

    protected ExceptionUnhandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return unhandled(doGet(getter), root);
    }
}
