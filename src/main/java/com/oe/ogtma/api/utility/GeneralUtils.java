package com.oe.ogtma.api.utility;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GeneralUtils {

    public static <T, U, V, R> TriFunction<T, U, V, R> triMemoize(final TriFunction<T, U, V, R> toMemo) {
        return new TriFunction<>() {

            private final Map<Triple<T, U, V>, R> cache = new ConcurrentHashMap<>();

            @Override
            public R apply(T t, U u, V v) {
                return this.cache.computeIfAbsent(Triple.of(t, u, v),
                        tri -> toMemo.apply(tri.getLeft(), tri.getMiddle(), tri.getRight()));
            }
        };
    }
}
