package org.haozhang.getty;

@FunctionalInterface
public interface GettyConstructor<T, G extends Getty<?>> {
    G newInstance(T object, Object root, boolean cached);
}
