# Getty
A Java getter-call chaining library
```java
Map<Integer, Integer> map = Collections.singletonMap(1, 1);

String value = Getty.of(map)
    .getOrDefault(m -> m.get(0), -1) // Default value
    .getNonNull(i -> null, (i, exception) -> { // Null value handling
        System.err.format("Exception occurred while calling getter on %d: %s", i, exception);
        return Double.NaN;
    })
    .get(Double::toHexString)
    .get();

System.out.println(value); // NaN
```


## Requirements
* Java 8+ (functional interfaces and lambda methods)
* Maven (**TODO:** make this a proper module)


## What is it?
Getty is a library that aims to help with chaining long getter calls on Java objects. It also adds
the ability to handle exceptions and/or null values at every link on the call-chain.

It uses no reflection whatsoever. Instead, the library mainly utilizes functional interfaces (and
subsequently lambda methods) that were introduced in Java 8.

Of course, its use isn't limited to this use case, but it was created with this purpose in mind.


## Why?
Consider the following getter call-chain to extract some data from a long chain of Java objects:
```java
Integer value = a.getB().getC().getD().getE();
```

You would now like to handle any `NullPointerException` that might occur during the call. You could
simply wrap the whole thing in a try-catch block:
```java
Integer value = null;

try {
    value = a.getB().getC().getD().getE();
} catch (NullPointerException npe) {
    // Ignore.
}
```

But perhaps you would like to specifically handle an exception that might occur during the call of
`getC()` while keeping the rest of the behavior identical.
```java
Integer value = null;

try {
    final B b = a.getB();
    C c = null;
    
    try {
        c = b.getC()
    } catch (Exception e) {
        // Do something with the exception.
        c = new C();
    }
    
    if (c != null) {
        // Finally, get the value we wanted.
        value = c.getD().getE();
    }
} catch (NullPointerException npe) {
    // Ignore.
}
```

Now suppose you would like to return your own instance of `D` if the `getD()` call returns null.
```java
Integer value = null;

try {
    final B b = a.getB();
    C c = null;
    
    try {
        c = b.getC()
    } catch (Exception e) {
        // Do something with the exception.
        c = new C();
    }
    
    if (c != null) {
        D d = c.getD();
        
        if (d == null) {
            // Use another instance.
            d = new D();
        }
        
        // Finally, get the value we wanted.
        value = d.getE();
    }
} catch (NullPointerException npe) {
    // Ignore.
}
```

Now imagine doing this for even more deeply-nested objects. What a nightmare!

This is where Getty could come in handy.

Let's revisit the code from before and go through their Getty-equivalents.

#### Handling NullPointerException for the entire getter-call chain
```java
Integer value = null;

try {
    value = a.getB().getC().getD().getE();
} catch (NullPointerException npe) {
    // Ignore.
}
```
```java
Integer value = Getty.of(a)
    .get(a -> a.getB().getC().getD().getE()) // Getty handles null values on the chain by default.
    .get();
```

#### Handling exceptions for a specific getter-call
```java
Integer value = null;

try {
    final B b = a.getB();
    C c = null;
    
    try {
        c = b.getC()
    } catch (Exception e) {
        // Do something with the exception.
        c = new C();
    }
    
    if (c != null) {
        // Finally, get the value we wanted.
        value = c.getD().getE();
    }
} catch (NullPointerException npe) {
    // Ignore.
}
```
```java
Integer value = Getty.of(a)
    .get(a -> a.getB())
    .get(b -> b.getC(), (b, exception) -> { // Make sure to isolate the getC() call.
        // At this point, you have the following options:
        // 1. Leave out the return statement altogether to automatically return null.
        // 2. Return an alternate value.
        // 3. Throw a new or rethrow the existing exception to immediately break out of the chain.
        return new C();
    })
    .get(c -> c.getD().getE())
    .get();
```

#### Returning a default value when a getter call returns null
```java
Integer value = null;

try {
    final B b = a.getB();
    C c = null;
    
    try {
        c = b.getC()
    } catch (Exception e) {
        // Do something with the exception.
        c = new C();
    }
    
    if (c != null) {
        D d = c.getD();
        
        if (d == null) {
            // Use another instance.
            d = new D();
        }
        
        // Finally, get the value we wanted.
        value = d.getE();
    }
} catch (NullPointerException npe) {
    // Ignore.
}
```
```java
Integer value = Getty.of(a)
    .get(a -> a.getB())
    .get(b -> b.getC(), (b, exception) -> new C())
    .getOrDefault(c -> c.getD(), () -> new D()) // This can either be a value, supplier, or function
    .get(d -> d.getE())
    .get();
```


## Isn't there already a solution to this problem?
If there is an existing library that solves this problem, then I am unaware of it. I just took the
idea given to me by a colleague and ran with it in my free time.

If you know of another library, do let me know! I'm always interested in learning different (and
perhaps better) ways of solving problems.


## Is performance a concern while using this library? 
Some considerations were taken to potentially optimize this library's use.

Getty instances created on a chain are cached using the value they hold and the root object used to
start the chain. Subsequent Getty instances are first queried from the cache before they are
instantiated.

Getty instances on a given chain are removed from the cache upon calling the appropriate terminal
getter method. Calling `get()` on a Getty instance will perform cache removal, but calling
`getAndCache()` will keep them cached.

You should prefer using `get()` for one-time chains:
```java
Integer value = Getty.of(a)
    .get(a -> a.getB().getC().getD().getE())
    .get();
```

If you want to reuse a Getty chain multiple times, consider using `getAndCache()` but remember to
call `get()` for the last use to remove the cache:
```java
Getty<A> a = Getty.of(a); // The root Getty chain which will be used multiple times

Getty<B> b1 = a.get(a -> a.getB());
Integer b1Value = b
    .get(b -> b.getC().getD().getE())
    .getAndCache(); // Keep the Getty instances created by this chain cached.

Getty<B> b2 = a.get(a -> a.getB()); // This returns the same instance as the one from b1.
Integer b2Value = b
    .get(b -> b.getC().getD().getE()) // The same goes for this one.
    .get(); // Remember to call the cache-removing terminal get() in the final use!

// No more usage of "a" below.
```

To see a more in-depth demonstration of the caching mechanism, take a look at the `chainCaching`
test case in [GettyTest.java](src/test/java/org/haozhang/getty/GettyTest.java).

## Is this library guaranteed to be thread-safe?
**Short answer: No.**

You should take great care with using this in highly-concurrent and time-critical environments.

Although I have taken some preliminary actions to lessen the potential problems of multi-threaded
use, I cannot guarantee the thread-safety of this library's operations.

The caching mechanism is built with *supposedly* thread-safe containers from the standard library
such as `ConcurrentHashMap`. I tried to make sure that I'm using all the atomic operation methods
such as `compute` and `computeIfAbsent()`. I am currently researching the thread-safety of their
element traversal and removal. 

If you find an issue with thread-safety, let me know or create a pull request with your fix.


## How do I use it?
**TODO:** Add examples

You can also refer to the [**Why**](#why) section above for more examples.
