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

    /**
     * Return a cached or uncached instance of {@link ExceptionHandledGetty} based on whether the
     *   given {@code root} has been cached.
     *
     * @param object The object to be held by this {@link ExceptionHandledGetty} instance
     * @param root The root object used to start this Getty chain
     * @param <T> The type of the object held by this {@link ExceptionHandledGetty} instance
     * @return An {@link ExceptionHandledGetty} instance holding the given object
     */
    protected static <T> ExceptionHandledGetty<T> getInstance(T object, Object root) {
        if (CACHE.containsKey(root)) {
            return getCachedInstance(object, root);
        }
        return getUncachedInstance(object, root);
    }

    protected static <T> ExceptionHandledGetty<T> getUncachedInstance(T object, Object root) {
        return new ExceptionHandledGetty(object, root);
    }

    @SuppressWarnings("unchecked")
    protected static <T> ExceptionHandledGetty<T> getCachedInstance(T object, Object root) {
        return (ExceptionHandledGetty<T>)
            getCachedInstance(object, root, ExceptionHandledGetty::new, CACHE);
    }
}
