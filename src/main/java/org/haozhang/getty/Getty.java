package org.haozhang.getty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.haozhang.getty.ExceptionHandlerFunction.RETURN_NULL;
import static org.haozhang.getty.ExceptionHandlerFunction.THROW_NULL_POINTER_EXCEPTION;

/**
 * This class provides a mechanism to chain long getter calls while adding additional abilities.
 * <br/>
 * TODO:
 *
 * @param <T> The type of the object held by this {@link Getty} instance
 */
public abstract class Getty<T> {
    /**
     * The cache holding Getty chains for reuse
     */
    protected static final Map<Object, GettyChain> CACHE = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

    /**
     * Whether the {@link Getty} instances should be cached by default when calling
     *   {@link #of(Object)}
     */
    private static final boolean CACHE_BY_DEFAULT = false;

    /**
     * Sentinel object used as a workaround for null keys and values in {@link ConcurrentHashMap}
     */
    private static final Object NULL_SENTINEL = new Object();

    /**
     * The object held by this {@link Getty} instance
     * <br/>
     *
     * Note: Do not use this directly as it may hold the special {@code NULL_SENTINEL} value; you
     *   should use {@link #rawGet()} instead.
     */
    protected final T object;

    /**
     * The Getty chain that this {@link Getty} instance belongs to
     * <br/>
     *
     * If this is set to {@code null}, then no caching is performed on this {@link Getty} instance.
     */
    protected final GettyChain chain;

    /**
     * Construct an instance of {@link Getty} with an object that it will hold, as well as the Getty
     *   chain that it will belong to, if any.
     * <br/>
     *
     * Getty chains are used as a caching mechanism. If {@code chain} is null, then no caching is
     *   done on this {@code Getty} instance.
     *
     * @param object The object to be held by this {@link Getty} instance
     * @param chain The Getty chain that this {@link Getty} instance belongs to; {@code null} if the
     *              chain is uncached
     */
    protected Getty(T object, GettyChain chain) {
        this.object = object;
        this.chain = chain;
    }

    //==============================================================================================
    // Terminal Chaining Methods
    //==============================================================================================

    /**
     * Return the object held by this {@link Getty} instance and remove all of the cached
     *   {@link Getty} instances created since the start of this Getty chain.
     * <br/>
     *
     * If you would like to keep those instances cached for later use, use {@link #getAndCache()}
     *   instead.
     *
     * @return The object held by this {@link Getty} instance
     */
    public T get() {
        if (null != chain) {
            uncacheChain();
        }
        return rawGet();
    }

    /**
     * Return the object held by this {@link Getty} instance but keep all of the {@link Getty}
     *   instances created since the start of this Getty chain cached.
     * <br/>
     *
     * If you would like to remove those instances from the cache, use {@link #get()} instead.
     *
     * @return The object held by this {@link Getty} instance
     */
    public T getAndCache() {
        return rawGet();
    }

    //==============================================================================================
    // Potentially Exception-unhandled Chaining Methods - Implementation-specific
    //==============================================================================================

    public abstract <R> Getty<R> get(Getter<T, R> getter);

    public abstract <R> Getty<R> getOrDefault(Getter<T, R> getter, R defaultValue);

    public abstract <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier
    );

    public abstract <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction
    );

    public abstract <R> Getty<R> getNonNull(Getter<T, R> getter);

    //==============================================================================================
    // Exception-handled Chaining Methods
    //==============================================================================================

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return chainHandled(rawGet(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return chainHandled(rawGet(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValue, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueFunction, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValue, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return chainHandled(rawGetOrDefault(getter, defaultValueFunction, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return chainHandled(rawGetNonNull(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return chainHandled(rawGetNonNull(getter, exceptionHandler));
    }

    //==============================================================================================
    // Chaining Helper Methods
    //==============================================================================================

    /**
     * Return a {@link Getty} instance on the same chain holding a given object.
     *
     * @param object
     * @param <R>
     * @return
     */
    protected <R> ExceptionUnhandledGetty<R> chainUnhandled(R object) {
        return ExceptionUnhandledGetty.getInstance(object, chain);
    }

    protected <R> ExceptionHandledGetty<R> chainHandled(R object) {
        return ExceptionHandledGetty.getInstance(object, chain);
    }

    //==============================================================================================
    // Getter Implementation Methods
    //==============================================================================================

    /**
     * Return the object held by this {@link Getty} instance while handling the null sentinel value.
     *
     * @return The object held by this {@link Getty} instance
     */
    protected T rawGet() {
        return object == NULL_SENTINEL ? null : object;
    }

    protected <R> R rawGet(Getter<T, R> getter) {
        return rawGet(getter, (ExceptionHandlerFunction<T, R>) RETURN_NULL);
    }

    protected <R> R rawGet(Getter<T, R> getter, ExceptionHandlerConsumer<T> exceptionHandler) {
        return rawGet(getter, exceptionHandler.toFunction());
    }

    protected <R> R rawGet(Getter<T, R> getter, ExceptionHandlerFunction<T, R> exceptionHandler) {
        try {
            return getter.apply(object);
        } catch (Exception exception) {
            return exceptionHandler.handleException(object, exception);
        }
    }

    /**
     * TODO:
     * This returns the default value when the getter returns null or an exception was thrown in the
     *   getter.
     *
     * @param getter
     * @param defaultValue
     * @param <R>
     * @return
     */
    protected <R> R rawGetOrDefault(Getter<T, R> getter, R defaultValue) {
        return rawGetOrDefault(getter, defaultValue, (object, exception) -> defaultValue);
    }

    protected <R> R rawGetOrDefault(Getter<T, R> getter, Supplier<R> defaultValueSupplier) {
        return rawGetOrDefault(
            getter,
            defaultValueSupplier,
            (object, exception) -> { return defaultValueSupplier.get(); }
        );
    }

    protected <R> R rawGetOrDefault(Getter<T, R> getter, Function<T, R> defaultValueFunction) {
        return rawGetOrDefault(
            getter,
            defaultValueFunction,
            (object, exception) -> { return defaultValueFunction.apply(object); }
        );
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGetOrDefault(getter, defaultValue, exceptionHandler.toFunction());
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler.toFunction());
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGetOrDefault(getter, defaultValueFunction, exceptionHandler.toFunction());
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).rawGet();
        return null != value ? value : defaultValue;
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).rawGet();
        return null != value ? value : defaultValueSupplier.get();
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).rawGet();
        return null != value ? value : defaultValueFunction.apply(object);
    }

    protected <R> R rawGetNonNull(Getter<T, R> getter) {
        return rawGetNonNull(getter, (ExceptionHandlerFunction<T, R>) THROW_NULL_POINTER_EXCEPTION);
    }

    protected <R> R rawGetNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGetNonNull(getter, exceptionHandler.toFunction());
    }

    protected <R> R rawGetNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).rawGet();
        if (null == value) {
            return exceptionHandler.handleException(object, new NullPointerException());
        }
        return value;
    }

    //==============================================================================================
    // Caching Helper Methods
    //==============================================================================================

    private void uncacheChain() {
        LOGGER.debug(
            "Removing chain from cache: object={}, head={}, chain={}",
            object, chain.getHead(), chain
        );
        chain.clear();
        CACHE.remove(chain.getHead());
    }

    protected static <T, G extends Getty<T>> G getCachedInstance(
        T object,
        GettyChain chain,
        GettyConstructor<T> constructor
    ) {
        return (G) chain.computeIfAbsent(null == object ? NULL_SENTINEL : object, __ ->
            constructor.newInstance(object, chain)
        );
    }

    //==============================================================================================
    // Simple Getter Methods - For One-off Uses
    //==============================================================================================

    /**
     * Call the given supplier and return its value. If the call fails, then return {@code null}.
     *
     * @param valueSupplier The supplier to call to retrieve the value
     * @param <T> The type of the value returned by {@code valueSupplier}
     * @return The value
     */
    public static <T> T get(Supplier<T> valueSupplier) {
        return getOrDefault(valueSupplier, (T) null);
    }

    /**
     * Call the given supplier and return its value. If the call fails, then return
     *   {@code defaultValue}.
     *
     * @param valueSupplier The supplier to call to retrieve the value
     * @param defaultValue The default value to return if the call to {@code valueSupplier} fails
     * @param <T> The type of the value returned by {@code valueSupplier}
     * @return The value
     */
    public static <T> T getOrDefault(Supplier<T> valueSupplier, T defaultValue) {
        try {
            return valueSupplier.get();
        } catch (Exception exception) {
            LOGGER.error("Getter call failed; returning default value: " + defaultValue, exception);
            return defaultValue;
        }
    }

    /**
     * Call the given supplier and return its value. If the call fails, then return
     *   {@code defaultValue}.
     *
     * @param valueSupplier The supplier to call to retrieve the value
     * @param defaultValueSupplier The secondary value supplier to call to retrieve the value if the
     *   call to {@code valueSupplier} fails
     * @param <T> The type of the value returned by {@code valueSupplier}
     * @return The value
     */
    public static <T> T getOrDefault(Supplier<T> valueSupplier, Supplier<T> defaultValueSupplier) {
        try {
            return valueSupplier.get();
        } catch (Exception exception) {
            LOGGER.error("Getter call failed; calling default value supplier", exception);
            return defaultValueSupplier.get();
        }
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    /**
     * Begin a Getty chain and return the head {@link Getty} instance.
     * <br/>
     *
     * This is the default main entry method for the {@link Getty} library.
     * <br/>
     *
     * Caching is performed if the {@code CACHE_BY_DEFAULT} field of {@link Getty} is set to
     *   {@code true}. If you would like to decide whether to cache or not, use
     *   {@link #uncached(Object)} or {@link #cached(Object)}.
     *
     * @param object The object to be held by the head of the Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #uncached(Object)
     * @see #cached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> of(T object) {
        return CACHE_BY_DEFAULT ? cached(object) : uncached(object);
    }

    /**
     * Begin a cached Getty chain and return the head {@link Getty} instance.
     *
     * @param object The object to be held by the head of the Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #cached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> uncached(T object) {
        Objects.requireNonNull(object, "The object cannot be null.");
        return ExceptionUnhandledGetty.getUncachedInstance(object);
    }

    /**
     * Begin an uncached Getty chain and return the head {@link Getty} instance.
     *
     * @param object The object to be held by the head of the Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #uncached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> cached(T object) {
        Objects.requireNonNull(object, "The object cannot be null.");
        return ExceptionUnhandledGetty.getCachedInstance(
            object,
            CACHE.computeIfAbsent(object, GettyChain::new)
        );
    }
}
