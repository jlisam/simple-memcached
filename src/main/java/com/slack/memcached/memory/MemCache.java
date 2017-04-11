package com.slack.memcached.memory;

import java.util.function.Function;

/**
 * Created by jlisam on 4/8/17.
 */
public interface MemCache<K, V> {

    V get(K k);

    Boolean cas(K k, V v, Function<V, Boolean> function);

    void set(K k, V v);

    boolean delete(K k);

    boolean containsKey(K k);

    long size();
}
