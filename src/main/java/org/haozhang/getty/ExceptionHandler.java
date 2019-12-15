package org.haozhang.getty;

@FunctionalInterface
public interface ExceptionHandler<R> {
    ExceptionHandler<?> RETURN_NULL = (object, exception) -> null;

    R handleException(Object object, Exception exception);
}
