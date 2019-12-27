package org.haozhang.getty;

/**
 * This functional interface represents an exception handler that accepts an object and an exception
 *   and returns nothing (having no {@code return} statement at all).
 *
 * @param <T> The type of the object consumed by the {@link ExceptionHandlerConsumer}
 */
@FunctionalInterface
public interface ExceptionHandlerConsumer<T> {
    void handleException(T object, Exception exception);

    /**
     * Return an {@link ExceptionHandlerFunction}-equivalent of this
     *   {@link ExceptionHandlerConsumer} which returns {@code null}.
     *
     * @param <R> The inferred return-type of the returned {@link ExceptionHandlerFunction}
     * @return An {@link ExceptionHandlerFunction}-equivalent of this
     *   {@link ExceptionHandlerConsumer}
     */
    default <R> ExceptionHandlerFunction<T, R> toFunction() {
        return (object, exception) -> { handleException(object, exception); return null; };
    }

    //==============================================================================================
    // Factory Methods
    //==============================================================================================

    /**
     * Return an {@link ExceptionHandlerConsumer} that rethrows the exception passed to it (as a
     *   {@link RuntimeException}).
     *
     * @param <T> The type of the object consumed by the {@link ExceptionHandlerConsumer}
     * @return An {@link ExceptionHandlerConsumer} that rethrows the exception passed to it
     */
    static <T> ExceptionHandlerConsumer<T> rethrowException() {
        return (object, exception) -> {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            }
            throw new RuntimeException(exception);
        };
    }

    /**
     * Return an {@link ExceptionHandlerConsumer} that throws a {@link NullPointerException}.
     *
     * @param <T> The type of the object consumed by the {@link ExceptionHandlerConsumer}
     * @return An {@link ExceptionHandlerConsumer} that throws a {@link NullPointerException}
     */
    static <T> ExceptionHandlerConsumer<T> throwNullPointerException() {
        return (object, exception) -> { throw new NullPointerException(); };
    }
}
