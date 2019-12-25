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
 * This class provides a mechanism to chain long getter calls while adding exception- and
 *   null-handling capabilities.
 * <br/><br/>
 *
 * ABC
 * <br/><br/>
 *
 * See below for an example usage:
 * <pre>{@code
 *   Map<Integer, Integer> map = Collections.singletonMap(1, 1);
 *
 *   String value = Getty.of(map)
 *       .getOrDefault(m -> m.get(0), -1) // Default value
 *       .getNonNull(i -> null, (i, exception) -> { // Null value handling
 *           System.err.format("Exception occurred while calling getter on %d: %s", i, exception);
 *           return Double.NaN;
 *       })
 *       .get(Double::toHexString)
 *       .get();
 *
 *   System.out.println(value); // NaN
 * }</pre>
 *
 * @param <T> The type of the object held by this {@link Getty} instance
 */
public abstract class Getty<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

    //==============================================================================================
    // Static Variables
    //==============================================================================================

    /**
     * The cache holding Getty chains for reuse
     */
    protected static final Map<Object, GettyChain> CACHE = new ConcurrentHashMap<>();

    /**
     * Whether the {@link Getty} instances should be cached by default when calling
     *   {@link #of(Object)}
     */
    private static final boolean CACHE_BY_DEFAULT = false;

    /**
     * A message indicating the incorrect use of {@code null} as the head of a Getty chain
     */
    private static final String NULL_HEAD_ERROR = "Getty chains cannot start with a null value.";

    /**
     * Sentinel object used as a workaround for null keys in {@link ConcurrentHashMap}
     */
    private static final Object NULL_SENTINEL = new Object();

    //==============================================================================================
    // Instance-related
    //==============================================================================================

    /**
     * The object held by this {@link Getty} instance
     */
    protected final T object;

    /**
     * The Getty chain that this {@link Getty} instance belongs to
     * <br/><br/>
     *
     * If this is set to {@code null}, then no caching is performed on this {@link Getty} instance.
     */
    protected final GettyChain chain;

    /**
     * Construct an instance of {@link Getty} with an object that it will hold, as well as the Getty
     *   chain that it will belong to, if any.
     * <br/><br/>
     *
     * Getty chains are used as a caching mechanism. If {@code chain} is null, then no caching is
     *   done on this {@code Getty} instance.
     *
     * @param object The object to be held by this {@link Getty} instance
     * @param chain The Getty chain that this {@link Getty} instance belongs to; {@code null} if the
     *   chain is uncached
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
     * <br/><br/>
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
        return object;
    }

    /**
     * Return the object held by this {@link Getty} instance but keep all of the {@link Getty}
     *   instances created since the start of this Getty chain cached.
     * <br/><br/>
     *
     * If you would like to remove those instances from the cache, use {@link #get()} instead.
     *
     * @return The object held by this {@link Getty} instance
     */
    public T getAndCache() {
        return object;
    }

    //==============================================================================================
    // Potentially Exception-unhandled Chaining Methods - Implementation-specific
    //==============================================================================================

    /**
     * Return a {@link Getty} instance holding the object returned by a given getter.
     *
     * @param getter The getter to call with the object held by this {@link Getty} instance
     * @param <R> The return type of {@code getter}
     * @return A {@link Getty} instance holding the object returned by {@code getter}
     */
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
        return handled(rawGet(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGet(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValue, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValueFunction, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValue, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGetOrDefault(getter, defaultValueFunction, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGetNonNull(getter, exceptionHandler));
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGetNonNull(getter, exceptionHandler));
    }

    //==============================================================================================
    // Chaining Helper Methods
    //==============================================================================================

    /**
     * Return an {@link ExceptionUnhandledGetty} instance on the same Getty chain holding a given
     *   object.
     * <br/><br/>
     *
     * This {@link ExceptionUnhandledGetty} instance is cached if the Getty chain was started with
     *   {@link #cached(Object)}.
     *
     * @param object The object held by the returned {@link ExceptionUnhandledGetty} instance
     * @param <R> The type of the object held by the returned {@link ExceptionUnhandledGetty}
     *   instance
     * @return An {@link ExceptionUnhandledGetty} instance on the same Getty chain holding the given
     *   object.
     */
    protected <R> ExceptionUnhandledGetty<R> unhandled(R object) {
        return ExceptionUnhandledGetty.getInstance(object, chain);
    }

    /**
     * Return an {@link ExceptionHandledGetty} instance on the same Getty chain holding a given
     *   object.
     * <br/><br/>
     *
     * This {@link ExceptionHandledGetty} instance is cached if the Getty chain was started with
     *   {@link #cached(Object)}.
     *
     * @param object The object held by the returned {@link ExceptionHandledGetty} instance
     * @param <R> The type of the object held by the returned {@link ExceptionHandledGetty} instance
     * @return An {@link ExceptionHandledGetty} instance on the same Getty chain holding the given
     *   object.
     */
    protected <R> ExceptionHandledGetty<R> handled(R object) {
        return ExceptionHandledGetty.getInstance(object, chain);
    }

    //==============================================================================================
    // Getter Implementation Methods
    //==============================================================================================

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
        final R value = get(getter, exceptionHandler).object;
        return null != value ? value : defaultValue;
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        return null != value ? value : defaultValueSupplier.get();
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
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
        final R value = get(getter, exceptionHandler).object;
        if (null == value) {
            return exceptionHandler.handleException(object, new NullPointerException());
        }
        return value;
    }

    //==============================================================================================
    // Caching Helper Methods
    //==============================================================================================

    /**
     * Remove the Getty chain that this {@link Getty} instance belongs to from the cache.
     */
    private void uncacheChain() {
        LOGGER.debug(
            "Removing chain from cache: object={}, head={}, chain={}",
            object, chain.getHead(), chain
        );
        chain.clear();
        CACHE.remove(chain.getHead(), chain);
    }

    /**
     * Return a cached {@link Getty} instance belonging to a given Getty chain holding a given
     *   object.
     * <br/><br/>
     *
     * Note that {@code chain} and {@code constructor} must not be null.
     *
     * @param object The object to be held by the returned {@link Getty} instance
     * @param chain The Getty chain holding the to-be-returned {@link Getty} instance
     * @param constructor
     * @param <T> The type of the object to be held by the returned {@link Getty} instance
     * @param <G> The type of the {@link Getty} instance to be constructed
     * @return A cached {@link Getty} instance belonging to the given Getty chain holding the given
     *   object
     */
    protected static <T, G extends Getty<T>> G getCachedInstance(
        T object,
        GettyChain chain,
        GettyConstructor<T> constructor
    ) {
        return (G) chain.computeIfAbsent(
            null == object ? (T) NULL_SENTINEL : object,
            __ -> constructor.newInstance(object, chain)
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
        return getOrDefault(valueSupplier, (Supplier<T>) () -> null);
    }

    /**
     * Call the given supplier and return its value. If the supplier returns {@code null} or the
     *   call fails, then return {@code defaultValue}.
     *
     * @param valueSupplier The supplier to call to retrieve the value
     * @param defaultValue The default value to return if the call to {@code valueSupplier} fails
     * @param <T> The type of the value returned by {@code valueSupplier}
     * @return The value
     */
    public static <T> T getOrDefault(Supplier<T> valueSupplier, T defaultValue) {
        return getOrDefault(valueSupplier, (Supplier<T>) () -> defaultValue);
    }

    /**
     * Call the given supplier and return its value. If the supplier returns {@code null} or the
     *   call fails, then call the default value supplier and return its value. If the call fails,
     *   then return {@code null}.
     *
     * @param valueSupplier The supplier to call to retrieve the value
     * @param defaultValueSupplier The secondary value supplier to call to retrieve the value if the
     *   call to {@code valueSupplier} fails
     * @param <T> The type of the value returned by {@code valueSupplier}
     * @return The value
     */
    public static <T> T getOrDefault(Supplier<T> valueSupplier, Supplier<T> defaultValueSupplier) {
        Objects.requireNonNull(valueSupplier, "The value supplier cannot be null.");
        Objects.requireNonNull(defaultValueSupplier, "The default value supplier cannot be null.");

        try {
            final T value = valueSupplier.get();
            if (null != value) {
                return value;
            }
        } catch (Exception exception) {
            LOGGER.error("Value supplier call failed; calling default value supplier", exception);
        }

        try {
            return defaultValueSupplier.get();
        } catch (Exception exception) {
            LOGGER.error("Default value supplier call failed", exception);
            return null;
        }
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    /**
     * Begin a Getty chain and return the head {@link Getty} instance.
     * <br/><br/>
     *
     * This is the default entry method for the {@link Getty} library.
     * <br/><br/>
     *
     * Caching is performed if the {@code CACHE_BY_DEFAULT} field of {@link Getty} is set to
     *   {@code true}. If you would like to decide whether to cache or not, use
     *   {@link #uncached(Object)} or {@link #cached(Object)}.
     *
     * @param head The object to be held by the head {@link Getty} instance of this Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #uncached(Object)
     * @see #cached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> of(T head) {
        return CACHE_BY_DEFAULT ? cached(head) : uncached(head);
    }

    /**
     * Begin an uncached Getty chain and return the head {@link Getty} instance.
     *
     * @param head The object to be held by the head {@link Getty} instance of this Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #cached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> uncached(T head) {
        Objects.requireNonNull(head, NULL_HEAD_ERROR);
        return ExceptionUnhandledGetty.getUncachedInstance(head);
    }

    /**
     * Begin a cached Getty chain and return the head {@link Getty} instance.
     *
     * @param head The object to be held by the head {@link Getty} instance of this Getty chain
     * @param <T> The type of the object held by this {@link Getty} instance
     * @return A {@link Getty} instance holding the given object
     *
     * @see #uncached(Object)
     */
    public static <T> ExceptionUnhandledGetty<T> cached(T head) {
        Objects.requireNonNull(head, NULL_HEAD_ERROR);
        return ExceptionUnhandledGetty.getCachedInstance(
            head,
            CACHE.computeIfAbsent(head, GettyChain::new)
        );
    }
}
