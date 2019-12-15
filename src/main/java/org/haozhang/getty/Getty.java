package org.haozhang.getty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.BiFunction;

@SuppressWarnings("unchecked")
public abstract class Getty<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Getty.class);

    protected final T object;
    protected final Object root;

    protected Getty(T object, Object root) {
        this.object = object;
        this.root = root;
    }

    public T get() {
        removeChainCache();
        return object;
    }

    public T getAndCache() {
        return object;
    }

    public abstract <R> Getty<R> get(Getter<T, R> getter);

    public <R> ExceptionHandledGetty<R> get(Getter<T, R> getter, ExceptionHandler<R> exceptionHandler) {
        return handled(doGet(getter, exceptionHandler), root);
    }

    protected <R> R doGet(Getter<T, R> getter) {
        return doGet(getter, (ExceptionHandler<R>) ExceptionHandler.RETURN_NULL);
    }

    protected <R> R doGet(Getter<T, R> getter, ExceptionHandler<R> exceptionHandler) {
        try {
            return getter.apply(object);
        } catch (Exception exception) {
            return exceptionHandler.handleException(object, exception);
        }
    }

    @SuppressWarnings("rawtypes")
    private void removeChainCache() {
        Map<Object, LinkedHashSet<Getty<?>>> cacheMap;
        if (ExceptionUnhandledGetty.class.equals(getClass())) {
            cacheMap = (Map) ExceptionUnhandledGetty.CACHE_MAP;
        } else {
            cacheMap = (Map) ExceptionHandledGetty.CACHE_MAP;
        }

        final Iterator<Map.Entry<Object, LinkedHashSet<Getty<?>>>> cacheIterator = cacheMap.entrySet().iterator();
        while (cacheIterator.hasNext()) {
            Map.Entry<Object, LinkedHashSet<Getty<?>>> cache = cacheIterator.next();
            if (cache.getKey() == root) {
                LOGGER.trace("Removing chain cache for {}: object={}, root={}, cache={}", this, object, root, cache.getValue());
                cache.getValue().clear();
                cacheIterator.remove();
                return;
            }
        }
    }

    public static <T> ExceptionUnhandledGetty<T> of(T object) {
        return unhandled(object, object);
    }

    protected static <T> ExceptionUnhandledGetty<T> unhandled(T object, Object root) {
        return (ExceptionUnhandledGetty<T>) getInstance(
            object,
            root,
            ExceptionUnhandledGetty::new,
            ExceptionUnhandledGetty.CACHE_MAP,
            ExceptionUnhandledGetty.NULL
        );
    }

    protected static <T> ExceptionHandledGetty<T> handled(T object, Object root) {
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
            return nullInstance;
        } else if (null == root) {
            root = object;
        }

        LinkedHashSet<G> cache = cacheMap.computeIfAbsent(root, _root ->
            new LinkedHashSet(Collections.singleton(constructor.apply(object, _root)))
        );

        G instance = cache.stream().filter(cachedGetty -> cachedGetty.object == object).findAny().orElse(null);
        if (null == instance) {
            instance = constructor.apply(object, root);
            cache.add(instance);
        }
        return instance;
    }
}
