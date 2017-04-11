package com.slack.memcached.memory;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jlisam on 4/10/17.
 */
public class LRUMemCacheIntegrationTests {

    @Test
    public void shouldSupportConcurrentSetting() throws Exception {

        LRUMemCache<String, Integer> memCache = new LRUMemCache<>(100);
        ExecutorService e = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 10000; i++) {
            e.submit(() -> {
                int r = ThreadLocalRandom.current().nextInt();
                memCache.set(Thread.currentThread().getName() + r, r);
            });
        }
        e.shutdown();
        e.awaitTermination(100, TimeUnit.SECONDS);

        assertEquals(memCache.size(), 100);
    }

    @Test
    public void shouldSupportConcurrentSettingAndGetting() throws Exception {
        LRUMemCache<String, String> memCache = new LRUMemCache<>(100);
        ExecutorService e = Executors.newFixedThreadPool(50);

        for (int i = 0; i < 10000; i++) {

            e.submit(() -> {
                Random random = new Random();
                Integer r = random.nextInt(3);
                String name = "Thread";
                memCache.set(name + r, r.toString());
                assertEquals(name + r, memCache.get(name + r));
                assertTrue(memCache.size() <= 100);
            });
        }

        e.shutdown();
        e.awaitTermination(100, TimeUnit.SECONDS);

        for (LRUCache<String, String> lruCache : memCache.getRegions()) {
            System.out.println(lruCache);
        }
    }

    @Test
    public void shouldSupportConcurrentRemoval() throws Exception {
        LRUMemCache<String, String> memCache = new LRUMemCache<>(100);
        ExecutorService e = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 10000; i++) {
            Random random = new Random();
            e.submit(() -> {
                Integer r = random.nextInt(3);
                String name = "Thread";
                memCache.set(name, r.toString());
                long cacheSize = memCache.size();
                memCache.delete(name);
                long newCacheSize = memCache.size();
                assertEquals(newCacheSize, cacheSize - 1);
            });
        }
    }
}
