package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

public class ExceptionHandledGetty<T> extends Getty<T> {
    /**
     * @see Getty#Getty(Object, GettyChain)
     */
    protected ExceptionHandledGetty(T object, GettyChain chain) {
        super(object, chain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter) {
        return handled(rawGet(getter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return handled(rawGetOrDefault(getter, defaultValue));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    ) {
        return handled(rawGetOrDefault(getter, defaultValueSupplier));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    ) {
        return handled(rawGetOrDefault(getter, defaultValueFunction));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> ExceptionHandledGetty<R> getNonNull(Getter<T, R> getter) {
        return handled(rawGetNonNull(getter));
    }

    /**
     * Return an {@link ExceptionHandledGetty} instance holding the given object.
     * <br/><br/>
     *
     * A cached {@link ExceptionHandledGetty} instance will be returned if {@code chain} is given.
     *   Otherwise, an uncached {@link ExceptionHandledGetty} instance will be returned.
     * <br/><br/>
     *
     * Note that this is strictly for internal use.
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
        return new ExceptionHandledGetty<T>(object, null);
    }

    protected static <T> ExceptionHandledGetty<T> getCachedInstance(T object, GettyChain chain) {
        return getCachedInstance(object, chain, ExceptionHandledGetty::new);
    }
}
