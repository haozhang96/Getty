package org.haozhang.getty;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public abstract class GettyTestSupport {
    //==============================================================================================
    // Commonly Used Variables
    //==============================================================================================

    // Keep a reference to the same cache as the one in Getty using reflection.
    protected static final Map<Object, GettyChain> CACHE = getGettyFieldValue("CACHE");

    // The name of the system property used to determine whether Getty.of() should cache instances
    protected static final String CACHE_DETERMINER = getGettyFieldValue("CACHE_DETERMINER");

    // Keys for the map below
    protected static final int GOOD_KEY = 1;
    protected static final int NULL_KEY = -1;

    // Expected values for the map below
    protected static final Integer GOOD_VALUE = 1;
    protected static final Integer NULL_VALUE = null;
    protected static final Integer DEFAULT_VALUE = 123;

    // The head object to start Getty chains with
    protected static final Map<Integer, Integer> MAP =
        Collections.singletonMap(GOOD_KEY, GOOD_VALUE);

    // Helpful map-getter lambda methods
    protected static final Getter<Map<Integer, Integer>, Integer> GOOD_GETTER =
        map -> map.get(GOOD_KEY);
    protected static final Getter<Map<Integer, Integer>, Integer> NULL_GETTER =
        map -> map.get(NULL_KEY);
    protected static final Getter<?, ?> BAD_GETTER =
        object -> { throw new RuntimeException(); };

    // Helpful suppliers
    protected static final Supplier<?> GOOD_SUPPLIER =
        () -> GOOD_VALUE;
    protected static final Supplier<?> NULL_SUPPLIER =
        () -> null;
    protected static final Supplier<?> BAD_SUPPLIER =
        () -> { throw new RuntimeException(); };

    //==============================================================================================
    // Helper Methods
    //==============================================================================================

    // Return the value of a field in the Getty class with a given name using reflection.
    private static <T> T getGettyFieldValue(String name) {
        try {
            final Field field = Getty.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(null);
        } catch (Exception exception) {
            return null;
        }
    }
}
