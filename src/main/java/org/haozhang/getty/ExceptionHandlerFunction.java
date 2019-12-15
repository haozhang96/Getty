package org.haozhang.getty;

@FunctionalInterface
public interface ExceptionHandlerFunction<T, R> {
    R handleException(T object, Exception exception);

    ExceptionHandlerFunction<?, ?> RETURN_NULL = (object, exception) -> null;
    ExceptionHandlerFunction<?, ?> THROW_NULL_POINTER_EXCEPTION =
        fromConsumer(ExceptionHandlerConsumer.THROW_NULL_POINTER_EXCEPTION);
    ExceptionHandlerFunction<?, ?> RETHROW_EXCEPTION =
        fromConsumer(ExceptionHandlerConsumer.RETHROW_EXCEPTION);

    static <T, R> ExceptionHandlerFunction<T, R> fromConsumer(
        ExceptionHandlerConsumer<T> exceptionHandler
    ) {
        return ExceptionHandlerConsumer.toFunction(exceptionHandler);
    }

    static <T, R> ExceptionHandlerConsumer<T> toConsumer(
        ExceptionHandlerFunction<T, R> exceptionHandler
    ) {
        return (object, exception) -> exceptionHandler.handleException(object, exception);
    }
}
