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
    protected static final Map<Object, Map<Object, Getty<?>>> CACHE = new ConcurrentHashMap<>();

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
     * Note to internal users: do not use this directly as it may hold the special
     *   {@code NULL_SENTINEL} value; you should use {@link #rawGet()} instead.
     */
    protected final T object;

    /**
     * The root object used to start this Getty chain (from the {@link #of(Object)} call)
     */
    protected final Object root;

    /**
     * Whether this {@link Getty} instance belongs on a cached Getty chain
     */
    protected final boolean cached;

    /**
     * Construct an instance of {@link Getty} with an object that it will hold, as well as a root
     *   object which should be the object used to start the Getty chain.
     * <br/>
     *
     * For example, consider the following Getty chain:
     * <pre>{@code
     *   Getty.of(map).get(m -> m.get(1)).get(Integer::doubleValue).get();
     * }</pre>
     *
     * All subsequent non-terminal {@link #get(Getter)} (and its varations) calls will, at some
     *   point, call this constructor with {@code map} as the {@code root} parameter.
     * <br/>
     *
     * This is used to keep track of the {@link Getty} instances created by the chain for removal
     *   upon calling an appropriate terminal method, such as {@link #get()}.
     *
     * @param object The object to be held by this {@link Getty} instance
     * @param root The root object used to start this Getty chain
     * @param cached Whether to cache this {@link Getty} instance
     */
    protected Getty(T object, Object root, boolean cached) {
        this.object = object;
        this.root = root;
        this.cached = cached;
    }

    //==============================================================================================
    // Terminal Chaining Methods
    //==============================================================================================

    /**
     * Return the object held by this {@link Getty} instance and remove all of the cached
     *   {@link Getty} instances created since the start of this Getty chain.
     * <br/>
     * If you would like to keep those instances cached for later use, use {@link #getAndCache()}
     *   instead.
     *
     * @return The object held by this {@link Getty} instance
     */
    public T get() {
        if (cached) {
            removeChainCache();
        }
        return rawGet();
    }

    /**
     * Return the object held by this {@link Getty} instance but keep all of the {@link Getty}
     *   instances created since the start of this Getty chain cached.
     * <br/>
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
        return ExceptionHandledGetty.getInstance(rawGet(getter, exceptionHandler), root, cached);
    }

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(rawGet(getter, exceptionHandler), root, cached);
    }


    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValue, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueFunction, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValue, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueFunction, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetNonNull(getter, exceptionHandler),
            root,
            cached
        );
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetNonNull(getter, exceptionHandler),
            root,
            cached
        );
    }

    //==============================================================================================
    // Chaining Helper Methods
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

    private void removeChainCache() {
        LOGGER.trace("Removing chain cache: getty={}, object={}, root={}", this, object, root);
        CACHE.computeIfPresent(root, (__, chainCache) -> {
            chainCache.clear();
            return null; // Mark the chain cache container for removal by the garbage collector.
        });
    }

    protected static <T, G extends Getty<T>> G getCachedInstance(
        T object,
        Object root,
        GettyConstructor<T, G> constructor
    ) {
        // Use the special null sentinel value for the ConcurrentHashMaps.
        if (null == object) {
            object = (T) NULL_SENTINEL;
        }

        // Update the cache multi-map atomically.
        // See https://dzone.com/articles/java-8-concurrenthashmap-atomic-updates.
        return (G) CACHE
            .computeIfAbsent(root, __ -> new ConcurrentHashMap<>())
            .computeIfAbsent(object, _object -> constructor.newInstance((T) _object, root, true));
    }

    //==============================================================================================
    // Simple Getter Methods
    //==============================================================================================

    /**
     * Call the given supplier and return its value. If the call fails, then return {@code null}.
     *
     * @param valueSupplier
     * @param <T>
     * @return
     */
    public static <T> T get(Supplier<T> valueSupplier) {
        return getOrDefault(valueSupplier, (T) null);
    }

    public static <T> T getOrDefault(Supplier<T> valueSupplier, T defaultValue) {
        try {
            return valueSupplier.get();
        } catch (Exception exception) {
            LOGGER.error("Getter call failed; returning default value: " + defaultValue, exception);
            return defaultValue;
        }
    }

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
        return ExceptionUnhandledGetty.getUncachedInstance(object, object);
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
        return ExceptionUnhandledGetty.getCachedInstance(object, object);
    }
}
