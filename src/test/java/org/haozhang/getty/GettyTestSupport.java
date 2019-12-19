package org.haozhang.getty;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class GettyTestSupport {
    protected static final int VALUE = 1;
    protected static final int GOOD_KEY = 1;
    protected static final int BAD_KEY = -1;

    private static final Map<Integer, Integer> MAP = Collections.singletonMap(GOOD_KEY, VALUE);
    private static final Getter<Map<Integer, Integer>, Integer> GOOD_GETTER = m -> m.get(GOOD_KEY);
    private static final Getter<Map<Integer, Integer>, Integer> BAD_GETTER = m -> m.get(BAD_KEY);

    protected static final Supplier<Integer> plainGetter_goodIndex = () -> MAP.get(GOOD_KEY);
    protected static final Supplier<Integer> plainGetter_badIndex = () -> MAP.get(BAD_KEY);
    protected static final Supplier<Integer> lambdaHelper_goodGetter = () -> GOOD_GETTER.apply(MAP);
    protected static final Supplier<Integer> lambdaHelper_badGetter = () -> BAD_GETTER.apply(MAP);
    protected static final Supplier<Integer> gettyCached_goodGetter = () -> Getty.cached(MAP).get(GOOD_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyCached_badGetter = () -> Getty.cached(MAP).get(BAD_GETTER).getAndCache();
    protected static final Supplier<Integer> gettyUncached_goodGetter = () -> Getty.uncached(MAP).get(GOOD_GETTER).get();
    protected static final Supplier<Integer> gettyUncached_badGetter = () -> Getty.uncached(MAP).get(BAD_GETTER).get();
}
