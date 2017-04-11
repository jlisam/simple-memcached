package com.slack.memcached.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class LRUMemCache<K, V> implements MemCache<K, V> {

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    private static final int DEFAULT_MAX_ENTRIES = 10_000;
    private final List<LRUCache<K, V>> regions;
    private final int regionLevels;

    public LRUMemCache() {
        this(DEFAULT_MAX_ENTRIES, DEFAULT_CONCURRENCY_LEVEL);
    }

    public LRUMemCache(int maxEntries) {
        this(maxEntries, DEFAULT_CONCURRENCY_LEVEL);
    }

    public LRUMemCache(int maxEntries, final int concurrencyLevel) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("Cache size cannot be less than or equal to 0");
        }
        regions = new ArrayList<>(concurrencyLevel);
        regionLevels = concurrencyLevel;
        int itemsPerBucket = -1;
        int remaining = 0;
        if ((maxEntries - 1) >= concurrencyLevel) {
            itemsPerBucket = (maxEntries - 1)/ concurrencyLevel;
            remaining = (maxEntries - 1) % concurrencyLevel;
        }

        for (int i = 0; i < regionLevels; i++) {
            if (itemsPerBucket == -1) {
                if (maxEntries > 1) {
                    regions.add(i, new LRUCache<>(1));
                    maxEntries--;
                }
            } else {
                int extra = (i <= remaining) ? 1 : 0;
                regions.add(i, new LRUCache<>(itemsPerBucket + extra));
            }
        }
    }

    private int regionIndex(K k) {
        return Math.abs(k.hashCode() % regionLevels);
    }

    private LRUCache<K, V> region(K k) {
        return regions.get(regionIndex(k));
    }

    @Override
    public V get(K k) {
        return region(k).get(k);
    }

    @Override
    public void set(K k, V v) {
        LRUCache<K, V> bucket = region(k);
        bucket.put(k, v);
    }

    @Override
    public synchronized Boolean cas(K k, V v, Function<V, Boolean> function) {
        V data = get(k);
        if (data == null) {
            return null;
        }
        boolean casOk = function.apply(data);
        if (casOk) {
            set(k, v);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(K k) {
        return (region(k).remove(k) != null);
    }

    @Override
    public long size() {
        synchronized (regions) {
            long size = 0;
            for (LRUCache<K, V> cache : regions) {
                size += cache.size();
            }
            return size;
        }
    }

    @Override
    public boolean containsKey(K k) {
        return region(k).containsKey(k);
    }

    LRUCache<K, V> getRegion(K k) {
        return region(k);
    }

    List<LRUCache<K, V>> getRegions() {
        return regions;
    }

    @Override
    public String toString() {
        synchronized (regions) {
            StringBuilder builder = new StringBuilder();
            for (LRUCache<K, V> cache : regions) {
                builder.append(cache.toString()).append('\n');
            }

            return builder.toString();

        }
    }
}
