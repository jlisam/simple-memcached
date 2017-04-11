package com.slack.memcached.memory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jlisam on 4/10/17.
 */
public class LRUMemCacheTests {


    @Test(expected = IllegalArgumentException.class)
    public void whenMaxEntryIsSetToZeroItShouldThrowException() {
        LRUMemCache<Integer, String> lruMemCache = new LRUMemCache<Integer, String>(0);
    }

    @Test
    public void whenSettingValueKeyShouldMapToRegion() {
        // one per bucket
        LRUMemCache<Integer, String> lruMemCache = new LRUMemCache<Integer, String>(16);
        String value = "Hello";
        Integer key = 1;
        lruMemCache.set(key, value);

        LRUCache<Integer, String> region = lruMemCache.getRegion(1);

        assertEquals(value, region.get(key));
    }
}
