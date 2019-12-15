package org.haozhang.getty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.BiFunction;

import static org.haozhang.getty.ExceptionHandlerFunction.RETURN_NULL;
import static org.haozhang.getty.ExceptionHandlerFunction.THROW_NULL_POINTER_EXCEPTION;

/**
 * This class provides a mechanism to chain long getter calls while adding
 *   additional abilities.
 * <br/>
 * TODO:
 *
 * @param <T> The type of the value currently held by this {@link Getty}
 *   instance (potentially on a chain)
 */
@SuppressWarnings("unchecked")
public abstract class Getty<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

    protected final T object;
    protected final Object root;

    /**
     * Construct an instance of {@link Getty} with a currently-bound object, as
     *   well as a root object which should be the object used to start the
     *   Getty chain.
     * <br/>
     *
     * For example, consider the following Getty chain:
     * <pre>{@code
     *   Getty.of(map).get(m -> m.get(1)).get(Integer::doubleValue).get();
     * }</pre>
     *
     * All subsequent non-terminal {@link #get(Getter)} (and its varations)
     *   calls will, at some point, call this constructor with {@code map} as
     *   the {@code root} parameter.
     * <br/>
     *
     * This is used to keep track of the Getty instances created by the chain
     *   for removal upon calling an appropriate terminal method (such as
     *   {@link #get()}).
     *
     * @param object The object to bind this {@link Getty} instance to
     * @param root The object used to start the Getty chain
     */
    protected Getty(T object, Object root) {
        this.object = object;
        this.root = null != root ? root : object;
    }

    //==========================================================================
    // Terminal Chaining Methods
    //==========================================================================

    /**
     * Retrieve the currently-bound object and remove all of the cached
     *   {@link Getty} instances created since the start of this Getty chain.
     * <br/>
     * If you would like to keep those instances cached for later use, use
     *   {@link #getAndCache()} instead.
     *
     * @return The currently-bound object
     */
    public T get() {
        removeChainCache();
        return object;
    }

    /**
     * Retrieve the currently-bound object but keep all of the {@link Getty}
     *   instances created since the start of this Getty chain cached.
     * <br/>
     * If you would like to remove those instances from the cache, use
     *   {@link #get()} instead.
     *
     * @return The currently-bound object
     */
    public T getAndCache() {
        return object;
    }

    //==========================================================================
    // Implementation-specific Chaining Methods
    //==========================================================================

    public abstract <R> Getty<R> get(Getter<T, R> getter);

    public abstract <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue
    );

    public abstract <R> Getty<R> getNonNull(Getter<T, R> getter);

    //==========================================================================
    // Generic Chaining Methods
    //==========================================================================

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGet(getter, exceptionHandler), root);
    }

    public <R> ExceptionHandledGetty<R> get(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGet(getter, exceptionHandler), root);
    }

    public <R> Getty<R> getOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(
            rawGetOrDefault(getter, defaultValue, exceptionHandler),
            root
        );
    }

    public <R> Getty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return handled(rawGetNonNull(getter, exceptionHandler), root);
    }

    public <R> Getty<R> getNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return handled(rawGetNonNull(getter, exceptionHandler), root);
    }

    //==========================================================================
    // Chaining Helper Methods
    //==========================================================================

    protected <R> R rawGet(Getter<T, R> getter) {
        return rawGet(getter, (ExceptionHandlerFunction<T, R>) RETURN_NULL);
    }

    protected <R> R rawGet(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGet(
            getter,
            ExceptionHandlerConsumer.toFunction(exceptionHandler)
        );
    }

    protected <R> R rawGet(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        try {
            return getter.apply(object);
        } catch (Exception exception) {
            return exceptionHandler.handleException(object, exception);
        }
    }

    /**
     * This returns the default value when the getter returns null or an
     *   exception was thrown in the getter.
     *
     * @param getter
     * @param defaultValue
     * @param <R>
     * @return
     */
    protected <R> R rawGetOrDefault(Getter<T, R> getter, R defaultValue) {
        return rawGetOrDefault(
            getter,
            defaultValue,
            (object, exception) -> defaultValue
        );
    }

    protected <R> R rawGetOrDefault(
        Getter<T, R> getter,
        R defaultValue,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        return null != value ? value : defaultValue;
    }

    protected <R> R rawGetNonNull(Getter<T, R> getter) {
        return rawGetNonNull(
            getter,
            (ExceptionHandlerFunction<T, R>) THROW_NULL_POINTER_EXCEPTION
        );
    }

    protected <R> R rawGetNonNull(
        Getter<T, R> getter,
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return rawGetNonNull(
            getter,
            ExceptionHandlerConsumer.toFunction(exceptionHandler)
        );
    }

    protected <R> R rawGetNonNull(
        Getter<T, R> getter,
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        final R value = get(getter, exceptionHandler).object;
        if (null == value) {
            return exceptionHandler.handleException(
                null,
                new NullPointerException()
            );
        }
        return value;
    }

    //==========================================================================
    // Other Helper Methods
    //==========================================================================

    @SuppressWarnings("rawtypes")
    private void removeChainCache() {
        Map<Object, LinkedHashSet<Getty<?>>> cacheMap;
        if (ExceptionUnhandledGetty.class.equals(getClass())) {
            cacheMap = (Map) ExceptionUnhandledGetty.CACHE_MAP;
        } else {
            cacheMap = (Map) ExceptionHandledGetty.CACHE_MAP;
        }

        final Iterator<Map.Entry<Object, LinkedHashSet<Getty<?>>>> iterator =
            cacheMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, LinkedHashSet<Getty<?>>> cache = iterator.next();
            if (cache.getKey() == root) {
                LOGGER.trace(
                    "Removing chain cache for {}: object={}, root={}, cache={}",
                    this, object, root, cache.getValue()
                );
                cache.getValue().clear();
                iterator.remove();
                return;
            }
        }
    }

    //==========================================================================
    // Factory Methods
    //==========================================================================

    /**
     * Begin a Getty chain and return the head.
     *
     * @param object The object to bind to the head of the Getty chain
     * @param <T>
     * @return
     */
    public static <T> ExceptionUnhandledGetty<T> of(T object) {
        return unhandled(object, object);
    }

    protected static <T> ExceptionUnhandledGetty<T> unhandled(
        T object,
        Object root
    ) {
        return (ExceptionUnhandledGetty<T>) getInstance(
            object,
            root,
            ExceptionUnhandledGetty::new,
            ExceptionUnhandledGetty.CACHE_MAP,
            ExceptionUnhandledGetty.NULL
        );
    }

    protected static <T> ExceptionHandledGetty<T> handled(
        T object,
        Object root
    ) {
        return (ExceptionHandledGetty<T>) getInstance(
            object,
            root,
            ExceptionHandledGetty::new,
            ExceptionHandledGetty.CACHE_MAP,
            ExceptionHandledGetty.NULL
        );
    }

    private static <T, G extends Getty<?>> G getInstance(
        T object,
        Object root,
        BiFunction<T, Object, G> constructor,
        Map<Object, LinkedHashSet<G>> cacheMap,
        G nullInstance
    ) {
        if (null == object) {
            // Return the special null-bounded Getty instance.
            return nullInstance;
        } else if (null == root) {
            // Use the to-be-bound object as the root of the new Getty chain.
            root = object;
        }

        //
        LinkedHashSet<G> cache = cacheMap.computeIfAbsent(root, _root ->
            new LinkedHashSet(Collections.singleton(
                constructor.apply(object, _root)
            ))
        );

        // Attempt to find a cached Getty instance bound to this object with the
        //   same root object.
        G instance = cache.stream()
            .filter(cachedGetty -> cachedGetty.object == object)
            .findAny().orElse(null);

        // If a Getty instance does not exist, create one and cache it.
        if (null == instance) {
            instance = constructor.apply(object, root);
            cache.add(instance);
        }

        return instance;
    }
}
