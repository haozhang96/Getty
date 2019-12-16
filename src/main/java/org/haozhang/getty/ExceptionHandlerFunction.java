package org.haozhang.getty;

@FunctionalInterface
public interface ExceptionHandlerFunction<T, R> {
    R handleException(T object, Exception exception);

    ExceptionHandlerFunction<?, ?> RETURN_NULL = (object, exception) -> null;
    ExceptionHandlerFunction<?, ?> THROW_NULL_POINTER_EXCEPTION =
        ExceptionHandlerConsumer.THROW_NULL_POINTER_EXCEPTION.toFunction();
    ExceptionHandlerFunction<?, ?> RETHROW_EXCEPTION =
        ExceptionHandlerConsumer.RETHROW_EXCEPTION.toFunction();

    default ExceptionHandlerConsumer<T> toConsumer() {
        return this::handleException; // I have no idea why this works. My IDE told me about it.
    }
}
