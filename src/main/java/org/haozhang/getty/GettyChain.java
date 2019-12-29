package org.haozhang.getty;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a Getty chain by using {@link ConcurrentHashMap} as a thread-safe
 *   container to hold the {@link Getty} instances belonging to the chain.
 * <br/><br/>
 *
 * A Getty chain contains {@link Getty} instances which all point to the same {@code head} that was
 *   used to start the chain.
 * <br/><br/>
 *
 * The constructor must not be called with a null value as there is no null-checking done by the
 *   other methods. As such, this class is limited to use within this package where this condition
 *   can be ensured.
 */
class GettyChain extends ConcurrentHashMap<Object, Getty<?>> {
    /**
     * The head object used to start this Getty chain (from the {@link Getty#of(Object)} call)
     */
    public final transient Object head;

    /**
     * Construct a {@link GettyChain} instance with a given head for the chain.
     *
     * @param head The head of the Getty chain; must not be null
     */
    public GettyChain(Object head) {
        this.head = head;
    }

    // Use our own simplified method instead of the expensive one in ConcurrentHashMap.
    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GettyChain && other.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return head.hashCode();
    }
}
