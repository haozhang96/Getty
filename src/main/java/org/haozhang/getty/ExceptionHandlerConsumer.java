package org.haozhang.getty;

@FunctionalInterface
public interface ExceptionHandlerConsumer<T> {
    void handleException(T object, Exception exception);

    ExceptionHandlerConsumer<?> THROW_NULL_POINTER_EXCEPTION =
        (object, exception) -> { throw new NullPointerException(); };
    ExceptionHandlerConsumer<?> RETHROW_EXCEPTION =
        (object, exception) -> { throw new RuntimeException(exception); };

    default <R> ExceptionHandlerFunction<T, R> toFunction() {
        return (object, exception) -> { handleException(object, exception); return null; };
    }
}
