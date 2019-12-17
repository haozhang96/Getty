package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandledGetty<T> extends Getty<T> {
    protected ExceptionHandledGetty(T object, Object root, boolean cached) {
        super(object, root, cached);
    }

    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return getInstance(rawGet(getter), root, cached);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return getInstance(rawGetOrDefault(getter, defaultValue), root, cached);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueSupplier), root, cached);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return getInstance(rawGetOrDefault(getter, defaultValueFunction), root, cached);
    }

    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return getInstance(rawGetNonNull(getter), root, cached);
    }

    /**
     * Return a cached or uncached instance of {@link ExceptionHandledGetty} based on the given
     *   {@code cached} argument.
     * <br/>
     *
     * Note that this is strictly for internal use and will not function properly if the cache has
     *   not already been set up for the {@code root} and {@code object} pair.
     *
     * @param object The object to be held by this {@link ExceptionHandledGetty} instance
     * @param root The root object used to start this Getty chain
     * @param cached Whether to cache this {@link ExceptionHandledGetty} instance
     * @param <T> The type of the object held by this {@link ExceptionHandledGetty} instance
     * @return An {@link ExceptionHandledGetty} instance holding the given object
     */
    protected static <T> ExceptionHandledGetty<T> getInstance(
        T object,
        Object root,
        boolean cached
    ) {
        return cached ? getCachedInstance(object, root) : getUncachedInstance(object, root);
    }

    protected static <T> ExceptionHandledGetty<T> getUncachedInstance(T object, Object root) {
        return new ExceptionHandledGetty(object, root, false);
    }

    protected static <T> ExceptionHandledGetty<T> getCachedInstance(T object, Object root) {
        return getCachedInstance(object, root, ExceptionHandledGetty::new);
    }
}
