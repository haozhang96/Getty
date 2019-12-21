package org.haozhang.getty;

import java.util.concurrent.ConcurrentHashMap;

public class GettyChain extends ConcurrentHashMap<Object, Getty<?>> {
    /**
     * The head object used to start this Getty chain (from the {@link Getty#of(Object)} call)
     */
    private final Object head;

    public GettyChain(Object head) {
        this.head = head;
    }

    public Object getHead() {
        return head;
    }
}
