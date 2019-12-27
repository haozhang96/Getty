package org.haozhang.getty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class is the main entry-point of the Getty library, which provides a mechanism to chain long
 *   getter calls while adding exception- and null-handling capabilities.
 * <br/><br/>
 *
 * The Getty library can be used in two ways:
 * <ol>
 *   <li>
 *     Simple getter methods - see {@link #get(Supplier)}, {@link #getOrDefault(Supplier, Object)},
 *       and {@link #getOrDefault(Supplier, Supplier)}
 *   </li>
 *   <li>
 *     Chained getter methods - see {@link #of(Object)}, {@link #uncached(Object)}, and
 *       {@link #cached(Object)}
 *   </li>
 * </ol>
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
public class Getty<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

    //==============================================================================================
    // Static Variables
    //==============================================================================================

    /**
     * The cache holding Getty chains for reuse
     */
    private static final Map<Object, GettyChain> CACHE = new ConcurrentHashMap<>();

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
    private final T object;

    /**
     * The Getty chain that this {@link Getty} instance belongs to
     * <br/><br/>
     *
     * If this is set to {@code null}, then no caching is performed on this {@link Getty} instance.
     */
    private final GettyChain chain;

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
    private Getty(T object, GettyChain chain) {
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
    // Non-terminal Chaining Methods
    //==============================================================================================

    /**
     * Return a {@link Getty} instance holding the object returned by a given getter.
     *
     * @param getter The getter to call with the object held by this {@link Getty} instance
     * @param <R> The return type of {@code getter}
     * @return A {@link Getty} instance holding the object returned by {@code getter}
     */
    public <R> Getty<R> get(Getter<T, R> getter) {
        return get(getter, ExceptionHandlerFunction.returnNull());
    }

    public <R> Getty<R> get(Getter<T, R> getter, ExceptionHandlerConsumer<T> exceptionHandler) {
        return get(getter, exceptionHandler.toFunction());
    }

    public <R> Getty<R> get(Getter<T, R> getter, ExceptionHandlerFunction<T, R> exceptionHandler) {
        try {
            return chain(getter.apply(object));
        } catch (Exception exception) {
            return chain(exceptionHandler.handleException(object, exception));
        }
    }

    /**
     * Return a {@link Getty} instance holding the object returned by a given getter. If the getter
     *   throws an exception or returns {@code null}, then return {@code defaultValue}.
     *
     * @param getter The getter to call with the object held by this {@link Getty} instance
     * @param defaultValue The default value to return if the call to {@code valueSupplier} fails or
     *   returns {@code null}
     * @param <R> The return type of {@code getter}
     * @return A {@link Getty} instance holding the object returned by {@code getter} or
     *   {@code defaultValue}
     */
    public <R> Getty<R> getOrDefault(Getter<T, R> getter, R defaultValue) {
        return getOrDefault(getter, defaultValue, (object, exception) -> defaultValue);
    }

    public <R> Getty<R> getOrDefault(Getter<T, R> getter, Supplier<R> defaultValueSupplier) {
        return getOrDefault(
            getter,
            defaultValueSupplier,
            ExceptionHandlerFunction.fromSupplier(defaultValueSupplier)
        );
    }

    public <R> Getty<R> getOrDefault(Getter<T, R> getter, Function<T, R> defaultValueFunction) {
        return getOrDefault(
            getter,
            defaultValueFunction,
            ExceptionHandlerFunction.fromFunction(defaultValueFunction)
        );
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return getOrDefault(getter, defaultValue, exceptionHandler.toFunction());
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return getOrDefault(getter, defaultValueSupplier, exceptionHandler.toFunction());
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return getOrDefault(getter, defaultValueFunction, exceptionHandler.toFunction());
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        return chain(null != value ? value : defaultValue);
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        return chain(null != value ? value : defaultValueSupplier.get());
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        return chain(null != value ? value : defaultValueFunction.apply(object));
    }

    public <R> Getty<R> getNonNull(Getter<T, R> getter) {
        return getNonNull(getter, ExceptionHandlerFunction.throwNullPointerException());
    }

    public <R> Getty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return getNonNull(getter, exceptionHandler.toFunction());
    }

    public <R> Getty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        if (null == value) {
            return chain(exceptionHandler.handleException(object, new NullPointerException()));
        }
        return chain(value);
    }

    //==============================================================================================
    // Helper Methods
    //==============================================================================================

    /**
     * Return a {@link Getty} instance on the same Getty chain that this {@link Getty} instance
     *   belongs to (if it exists) holding a given object.
     * <br/><br/>
     *
     * The returned {@link Getty} instance will be cached if the Getty chain was started with
     *   {@link #cached(Object)}.
     *
     * @param object The object held by the returned {@link Getty} instance
     * @param <R> The type of the object held by the returned {@link Getty} instance
     * @return A {@link Getty} instance on the same Getty chain holding the given object
     */
    private <R> Getty<R> chain(R object) {
        return null != chain ? getCachedInstance(object, chain) : getUncachedInstance(object);
    }

    /**
     * Remove the Getty chain that this {@link Getty} instance belongs to from the cache.
     * <br/><br/>
     *
     * This should only be called if {@link #chain} is not {@code null}.
     */
    private void uncacheChain() {
        LOGGER.debug(
            "Removing chain from cache: object={}, head={}, chain={}",
            object, chain.head, chain
        );
        chain.clear();
        CACHE.remove(chain.head, chain);
    }

    /**
     * Return an uncached {@link Getty} instance holding a given object.
     *
     * @param object The object to be held by the returned {@link Getty} instance
     * @param <T> The type of the object to be held by the returned {@link Getty} instance
     * @return An uncached {@link Getty} instance holding the given object
     */
    private static <T> Getty<T> getUncachedInstance(T object) {
        return new Getty<>(object, null);
    }

    /**
     * Return a cached {@link Getty} instance belonging to a given Getty chain holding a given
     *   object.
     *
     * @param object The object to be held by the returned {@link Getty} instance
     * @param chain The Getty chain holding the to-be-returned {@link Getty} instance
     * @param <T> The type of the object to be held by the returned {@link Getty} instance
     * @return A cached {@link Getty} instance belonging to the given Getty chain holding the given
     *   object
     */
    private static <T> Getty<T> getCachedInstance(T object, GettyChain chain) {
        return (Getty<T>) chain.computeIfAbsent(
            null == object ? (T) NULL_SENTINEL : object,
            __ -> new Getty<>(object, chain)
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
     * @param defaultValue The default value to return if the call to {@code valueSupplier} fails or
     *   returns {@code null}
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
     *   call to {@code valueSupplier} fails or returns {@code null}
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
    public static <T> Getty<T> of(T head) {
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
    public static <T> Getty<T> uncached(T head) {
        Objects.requireNonNull(head, NULL_HEAD_ERROR);
        return getUncachedInstance(head);
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
    public static <T> Getty<T> cached(T head) {
        Objects.requireNonNull(head, NULL_HEAD_ERROR);
        return getCachedInstance(head, CACHE.computeIfAbsent(head, GettyChain::new));
    }
}
