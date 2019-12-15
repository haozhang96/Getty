package org.haozhang.getty;

@FunctionalInterface
public interface ExceptionHandlerConsumer<T> {
    void handleException(T object, Exception exception);

    ExceptionHandlerConsumer<?> THROW_NULL_POINTER_EXCEPTION =
        (object, exception) -> { throw new NullPointerException(); };
    ExceptionHandlerConsumer<?> RETHROW_EXCEPTION =
        (object, exception) -> { throw new RuntimeException(exception); };

    static <T, R> ExceptionHandlerConsumer<T> fromFunction(
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return ExceptionHandlerFunction.toConsumer(exceptionHandler);
    }

    static <T, R> ExceptionHandlerFunction<T, R> toFunction(
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return (object, exception) -> {
            exceptionHandler.handleException(object, exception);
            return null;
        };
    }
}
