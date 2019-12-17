package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected ExceptionUnhandledGetty(T object, Object root, boolean cached) {
        super(object, root, cached);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return getInstance(rawGet(getter), root, cached);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return getInstance(rawGetOrDefault(getter, defaultValue), root, cached);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueSupplier), root, cached);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueFunction), root, cached);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getNonNull(Getter<T, R> getter) {
        return getInstance(rawGetNonNull(getter), root, cached);
    }

    /**
     * Return a cached or uncached instance of {@link ExceptionUnhandledGetty} based on the given
     *   {@code cached} argument.
     * <br/>
     *
     * Note that this is strictly for internal use and will not function properly if the cache has
     *   not already been set up for the {@code root} and {@code object} pair.
     *
     * @param object The object to be held by this {@link ExceptionUnhandledGetty} instance
     * @param root The root object used to start this Getty chain
     * @param cached Whether to cache this {@link ExceptionUnhandledGetty} instance
     * @param <T> The type of the object held by this {@link ExceptionUnhandledGetty} instance
     * @return An {@link ExceptionUnhandledGetty} instance holding the given object
     */
    protected static <T> ExceptionUnhandledGetty<T> getInstance(
        T object,
        Object root,
        boolean cached
    ) {
        return cached ? getCachedInstance(object, root) : getUncachedInstance(object, root);
    }

    protected static <T> ExceptionUnhandledGetty<T> getUncachedInstance(T object, Object root) {
        return new ExceptionUnhandledGetty(object, root, false);
    }

    protected static <T> ExceptionUnhandledGetty<T> getCachedInstance(T object, Object root) {
        return getCachedInstance(object, root, ExceptionUnhandledGetty::new);
    }
}
