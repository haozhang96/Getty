package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected ExceptionUnhandledGetty(T object, GettyChain chain) {
        super(object, chain);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return chainUnhandled(rawGet(getter));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return chainUnhandled(rawGetOrDefault(getter, defaultValue));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return chainUnhandled(rawGetOrDefault(getter, defaultValueSupplier));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return chainUnhandled(rawGetOrDefault(getter, defaultValueFunction));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getNonNull(Getter<T, R> getter) {
        return chainUnhandled(rawGetNonNull(getter));
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
     * @param chain The {@link GettyChain} that this {@link ExceptionHandledGetty} instance is a
     *              part of
     * @param <T> The type of the object held by this {@link ExceptionUnhandledGetty} instance
     * @return An {@link ExceptionUnhandledGetty} instance holding the given object
     */
    protected static <T> ExceptionUnhandledGetty<T> getInstance(T object, GettyChain chain) {
        return null != chain ? getCachedInstance(object, chain) : getUncachedInstance(object);
    }

    protected static <T> ExceptionUnhandledGetty<T> getUncachedInstance(T object) {
        return new ExceptionUnhandledGetty(object, null);
    }

    protected static <T> ExceptionUnhandledGetty<T> getCachedInstance(T object, GettyChain chain) {
        return getCachedInstance(object, chain, ExceptionUnhandledGetty::new);
    }
}
