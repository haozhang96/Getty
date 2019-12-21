package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionUnhandledGetty<T> extends Getty<T> {
    protected ExceptionUnhandledGetty(T object, GettyChain chain) {
        super(object, chain);
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> get(Getter<T, R> getter) {
        return unhandled(rawGet(getter));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return unhandled(rawGetOrDefault(getter, defaultValue));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return unhandled(rawGetOrDefault(getter, defaultValueSupplier));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return unhandled(rawGetOrDefault(getter, defaultValueFunction));
    }

    @Override
    public <R> ExceptionUnhandledGetty<R> getNonNull(Getter<T, R> getter) {
        return unhandled(rawGetNonNull(getter));
    }

    /**
     * Return an {@link ExceptionUnhandledGetty} instance holding the given object.
     * <br/>
     *
     * A cached {@link ExceptionUnhandledGetty} instance will be returned if {@code chain} is given.
     *   Otherwise, an uncached {@link ExceptionUnhandledGetty} instance will be returned.
     * <br/>
     *
     * Note that this is strictly for internal use.
     *
     * @param object The object to be held by this {@link ExceptionUnhandledGetty} instance
     * @param chain The {@link GettyChain} that this {@link ExceptionUnhandledGetty} instance is a
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
