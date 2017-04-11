package com.slack.memcached.memory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;
    private int putCount;
    private int evictionCount;
    private int hitCount;
    private int missCount;

    public LRUCache(int maxSize) {
        super(16, 0.75f, true);
        this.maxSize = maxSize;
    }

    @Override
    public V get(Object key) {
        V value;
        synchronized (this) {
            value = super.get(key);
            if (value != null) {
                hitCount++;
                return value;
            }
            missCount++;
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        synchronized (this) {
            putCount++;
            V result = super.put(key, value);
            return result;
        }
    }


    @Override
    protected boolean removeEldestEntry ( final Map.Entry<K, V> eldest ) {
        synchronized (this) {
            boolean removeEldestEntry = super.size () > maxSize;
            if (removeEldestEntry) {
                evictionCount++;
            }
            return removeEldestEntry;
        }
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        synchronized (this) {
            return super.entrySet();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        synchronized (this) {
            return super.containsKey(key);
        }
    }

    /**
     * Removes the entry for {@code key} if it exists.
     *
     * @return the previous value mapped by {@code key}.
     */
    @Override
    public V remove(Object key) {
        synchronized (this) {
            return super.remove(key);
        }
    }

    @Override
    public int size() {
        synchronized (this) {
            return super.size();
        }
    }


    public synchronized final int maxSize() {
        return maxSize;
    }

    /**
     * Returns the number of times {@link #get} returned a value that was already present in the
     * cache.
     */
    public synchronized final int hitCount() {
        return hitCount;
    }

    /**
     * Returns the number of times {@link #get} returned null or required a new value to be
     * created.
     */
    public synchronized final int missCount() {
        return missCount;
    }

    /**
     * Returns the number of times {@link #put} was called.
     */
    public synchronized final int putCount() {
        return putCount;
    }

    /**
     * Returns the number of values that have been evicted.
     */
    public synchronized final int evictionCount() {
        return evictionCount;
    }

    @Override
    public synchronized String toString() {
        int accesses = hitCount + missCount;
        int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
        return String.format("LRUCache[maxSize=%d,hits=%d,misses=%d,size=%d,hitRate=%d%%]",
                maxSize, hitCount, missCount, size(), hitPercent);
    }
}