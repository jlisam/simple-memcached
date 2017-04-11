package com.slack.memcached.memory;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class LRUCacheTests {

    @Test
    public void whenCacheIsFullItemWillBeEvicted() {
        LRUCache<Integer, String> lruCache = new LRUCache<>(2);
        lruCache.put(1, "Hello");
        lruCache.put(2, "John");

        assertTrue(lruCache.size() == 2);

        lruCache.put(3, "Oscar");

        assertEquals(lruCache.evictionCount(), 1);
        assertEquals(lruCache.entrySet().iterator().next().getValue(), "John");
    }

    @Test
    public void whenCacheHitsAndMissStatisticsShouldMatch() {
        LRUCache<Integer, String> lruCache = new LRUCache<>(100);
        for (int i = 0; i < 100; i++) {
            lruCache.put(i, UUID.randomUUID().toString());
        }

        lruCache.get(101);
        assertEquals(lruCache.missCount(), 1);
        assertEquals(lruCache.putCount(), 100);

        String val = lruCache.get(99);
        String val1 = lruCache.get(98);
        String val2 = lruCache.get(96);

        assertNotNull(val);
        assertNotNull(val1);
        assertNotNull(val2);

        assertEquals(lruCache.hitCount(), 3);
    }
}
