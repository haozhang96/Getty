package org.haozhang.getty;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This functional interface represents an exception handler that accepts an object and an exception
 *   and returns a value.
 *
 * @param <T> The type of the object passed to the {@link ExceptionHandlerFunction}
 * @param <R> The type of the object returned by the {@link ExceptionHandlerFunction}
 */
@FunctionalInterface
public interface ExceptionHandlerFunction<T, R> {
    R handleException(T object, Exception exception);

    //==============================================================================================
    // Conversion Methods
    //==============================================================================================

    /**
     * Return an {@link ExceptionHandlerFunction}-equivalent of a given {@link Supplier}.
     *
     * @param supplier The {@link Supplier} to convert from
     * @param <T> The type of the object passed to the returned {@link ExceptionHandlerFunction}
     * @param <R> The type of the object returned by the returned {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction}-equivalent of the given {@link Supplier}
     */
    static <T, R> ExceptionHandlerFunction<T, R> fromSupplier(Supplier<R> supplier) {
        return (object, exception) -> supplier.get();
    }

    /**
     * Return an {@link ExceptionHandlerFunction}-equivalent of a given {@link Function}.
     *
     * @param function The {@link Function} to convert from
     * @param <T> The type of the object passed to the returned {@link ExceptionHandlerFunction}
     * @param <R> The type of the object returned by the returned {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction}-equivalent of the given {@link Function}
     */
    static <T, R> ExceptionHandlerFunction<T, R> fromFunction(Function<T, R> function) {
        return (object, exception) -> function.apply(object);
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    /**
     * Return an {@link ExceptionHandlerFunction} that returns null.
     *
     * @param <T> The type of the object passed to the {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction} that returns null
     */
    static <T, R> ExceptionHandlerFunction<T, R> returnNull() {
        return (object, exception) -> null;
    }

    /**
     * Return an {@link ExceptionHandlerFunction} that rethrows the exception passed to it (as a
     *   {@link RuntimeException}).
     *
     * @param <T> The type of the object passed to the {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction} that rethrows the exception passed to it
     */
    static <T, R> ExceptionHandlerFunction<T, R> rethrowException() {
        return ExceptionHandlerConsumer.<T>rethrowException().toFunction();
    }

    /**
     * Return an {@link ExceptionHandlerFunction} that throws a {@link NullPointerException}.
     *
     * @param <T> The type of the object passed to the {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction} that throws a {@link NullPointerException}
     */
    static <T, R> ExceptionHandlerFunction<T, R> throwNullPointerException() {
        return ExceptionHandlerConsumer.<T>throwNullPointerException().toFunction();
    }
}
