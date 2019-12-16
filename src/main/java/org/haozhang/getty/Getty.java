package org.haozhang.getty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.haozhang.getty.ExceptionHandlerFunction.RETURN_NULL;
import static org.haozhang.getty.ExceptionHandlerFunction.THROW_NULL_POINTER_EXCEPTION;

/**
 * This class provides a mechanism to chain long getter calls while adding additional abilities.
 * <br/>
 * TODO:
 *
 * @param <T> The type of the object held by this {@link Getty} instance (potentially on a chain)
 */
@SuppressWarnings("unchecked")
public abstract class Getty<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

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
     * The root object used to start this {@link Getty} chain (from the {@link #of(Object)} call)
     */
    protected final Object root;

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
     */
    protected Getty(T object, Object root) {
        this.object = object;
        this.root = root;
    }

    //==============================================================================================
    // Terminal Chaining Methods
    //==============================================================================================

    /**
     * Retrieve the object held by this {@link Getty} instance and remove all of the cached
     *   {@link Getty} instances created since the start of this Getty chain.
     * <br/>
     * If you would like to keep those instances cached for later use, use {@link #getAndCache()}
     *   instead.
     *
     * @return The object held by this {@link Getty} instance
     */
    public T get() {
        removeChainCache();
        return rawGet();
    }

    /**
     * Retrieve the object held by this {@link Getty} instance but keep all of the {@link Getty}
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
    // Potentially Exception-unhandled Chaining Methods
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
        return ExceptionHandledGetty.getInstance(rawGet(getter, exceptionHandler), root);
    }

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(rawGet(getter, exceptionHandler), root);
    }


    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValue, exceptionHandler),
            root
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler),
            root
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueFunction, exceptionHandler),
            root
        );
    }


    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValue, exceptionHandler),
            root
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Supplier<R> defaultValueSupplier,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueSupplier, exceptionHandler),
            root
        );
    }

    public <R> ExceptionHandledGetty<R> getOrDefault(
        Getter<T, R> getter,
        Function<T, R> defaultValueFunction,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(
            rawGetOrDefault(getter, defaultValueFunction, exceptionHandler),
            root
        );
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(rawGetNonNull(getter, exceptionHandler), root);
    }

    public <R> ExceptionHandledGetty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandledGetty.getInstance(rawGetNonNull(getter, exceptionHandler), root);
    }

    //==============================================================================================
    // Chaining Helper Methods
    //==============================================================================================

    /**
     * Retrieve the object held by this {@link Getty} instance while handling the null sentinel
     *   value.
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
            return exceptionHandler.handleException(null, new NullPointerException());
        }
        return value;
    }

    //==============================================================================================
    // Other Helper Methods
    //==============================================================================================

    protected abstract Map<Object, Map<Object, Getty<Object>>> getCache();

    private void removeChainCache() {
        LOGGER.trace("Removing chain cache: getty={}, object={}, root={}", this, object, root);
        getCache().computeIfPresent(root, (__, chainCache) -> {
            chainCache.clear();
            return null; // Mark the cache chain container for removal by the garbage collector.
        });
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    /**
     * Begin a Getty chain and return the head.
     * <br/>
     *
     * This is the main entry method for the {@link Getty} library.
     *
     * @param object The object to be held by the head of the Getty chain
     * @param <T>
     * @return
     */
    public static <T> ExceptionUnhandledGetty<T> of(T object) {
        Objects.requireNonNull(object, "The object cannot be null.");
        return ExceptionUnhandledGetty.getInstance(object, object);
    }

    /**
     *
     * <br/>
     *
     * This is for internal use only.
     *
     * @param object
     * @param root
     * @param constructor
     * @param cache
     * @param <T>
     * @param <G>
     * @return
     */
    protected static <T, G extends Getty<?>> G getInstance(
        T object,
        Object root,
        BiFunction<T, Object, G> constructor,
        Map<Object, Map<Object, G>> cache
    ) {
        // Use the special null sentinel value for the ConcurrentHashMaps.
        if (null == object) {
            object = (T) NULL_SENTINEL;
        }

        // Update the cache multi-map atomically.
        // See https://dzone.com/articles/java-8-concurrenthashmap-atomic-updates.
        return cache
            .computeIfAbsent(root, __ -> new ConcurrentHashMap<>())
            .computeIfAbsent(object, _object -> constructor.apply((T) _object, root));
    }
}
