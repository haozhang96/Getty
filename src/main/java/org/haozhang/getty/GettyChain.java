package org.haozhang.getty;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a Getty chain by using {@link ConcurrentHashMap} as a thread-safe
 *   container to hold the {@link Getty} instances belonging to the chain.
 * <br/><br/>
 *
 * A Getty chain contains {@link Getty} instances which all point to the same {@code head} that was
 *   used to start the chain.
 */
public class GettyChain extends ConcurrentHashMap<Object, Getty<?>> {
    /**
     * The head object used to start this Getty chain (from the {@link Getty#of(Object)} call)
     */
    public transient final Object head;

    public GettyChain(Object head) {
        this.head = head;
    }
}
