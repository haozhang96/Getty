package org.haozhang.getty;

@FunctionalInterface
public interface GettyConstructor<T> {
    Getty<T> newInstance(T object, GettyChain chain);
}
