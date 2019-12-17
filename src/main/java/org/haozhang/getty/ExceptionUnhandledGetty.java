package org.haozhang.getty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected static final Map<Object, Map<Object, ExceptionUnhandledGetty<Object>>> CACHE =
        new ConcurrentHashMap<>();

    protected ExceptionUnhandledGetty(T object, Object root) {
        super(object, root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return getInstance(rawGet(getter), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return getInstance(rawGetOrDefault(getter, defaultValue), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueSupplier), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueFunction), root);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getNonNull(Getter<T, R> getter) {
        return getInstance(rawGetNonNull(getter), root);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<Object, Map<Object, Getty<Object>>> getCache() {
        return (Map) CACHE;
    }

    /**
     * Return a cached or uncached instance of {@link ExceptionUnhandledGetty} based on whether the
     *   given {@code root} has been cached.
     *
     * @param object The object to be held by this {@link ExceptionUnhandledGetty} instance
     * @param root The root object used to start this Getty chain
     * @param <T> The type of the object held by this {@link ExceptionUnhandledGetty} instance
     * @return An {@link ExceptionUnhandledGetty} instance holding the given object
     */
    protected static <T> ExceptionUnhandledGetty<T> getInstance(T object, Object root) {
        if (CACHE.containsKey(root)) {
            return getCachedInstance(object, root);
        }
        return getUncachedInstance(object, root);
    }

    protected static <T> ExceptionUnhandledGetty<T> getUncachedInstance(T object, Object root) {
        return new ExceptionUnhandledGetty(object, root);
    }

    @SuppressWarnings("unchecked")
    protected static <T> ExceptionUnhandledGetty<T> getCachedInstance(T object, Object root) {
        return (ExceptionUnhandledGetty<T>)
            getCachedInstance(object, root, ExceptionUnhandledGetty::new, CACHE);
    }
}
