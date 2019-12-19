package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandledGetty<T> extends Getty<T> {
    protected ExceptionHandledGetty(T object, GettyChain chain) {
        super(object, chain);
    }

    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return chainHandled(rawGet(getter));
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return chainHandled(rawGetOrDefault(getter, defaultValue));
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueSupplier));
    }

    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueFunction));
    }

    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return chainHandled(rawGetNonNull(getter));
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
     * @param chain The {@link GettyChain} that this {@link ExceptionHandledGetty} instance is a
     *              part of
     * @param <T> The type of the object held by this {@link ExceptionHandledGetty} instance
     * @return An {@link ExceptionHandledGetty} instance holding the given object
     */
    protected static <T> ExceptionHandledGetty<T> getInstance(T object, GettyChain chain) {
        return null != chain ? getCachedInstance(object, chain) : getUncachedInstance(object);
    }

    protected static <T> ExceptionHandledGetty<T> getUncachedInstance(T object) {
        return new ExceptionHandledGetty(object, null);
    }

    protected static <T> ExceptionHandledGetty<T> getCachedInstance(T object, GettyChain chain) {
        return getCachedInstance(object, chain, ExceptionHandledGetty::new);
    }
}
