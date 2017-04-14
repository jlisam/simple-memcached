package com.slack.memcached.server;

import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by jlisam on 4/13/17.
 */
public class MemcachedServerIntegrationTests {

    private static MemcachedClient textMemcachedClient;
    private static final int TEST_PORT = 5000;

    private static Thread thread;

    @BeforeClass
    public static void setUp() throws Exception {
        thread = new Thread(() -> {

            Launcher launcher = new Launcher();
            try {
                launcher.run(new String[]{"-p", String.valueOf(TEST_PORT)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        textMemcachedClient = new MemcachedClient(new InetSocketAddress(TEST_PORT));
        Thread.sleep(5000);

    }

    @Test
    public void shouldReturnNullWhenNoResultFromGet() throws InterruptedException, IOException {
        assertNull(textMemcachedClient.get("hello"));
    }

    @Test
    public void canSetStringValueAndGetToMemcache() throws IOException, ExecutionException, InterruptedException {
        OperationFuture<Boolean> success = textMemcachedClient.set("key1", 0, "world");
        assertTrue(success.get());
        String returnedValue = (String) textMemcachedClient.get("key1");
        assertEquals(returnedValue, "world");
    }

    @Test
    public void shouldRetrieveSerializedValue() throws ExecutionException, InterruptedException {
        SomeValue input = new SomeValue(1, "world");
        OperationFuture<Boolean> success = textMemcachedClient.set("somekey", 0, input);
        assertTrue(success.get());
        SomeValue returnedValue = (SomeValue) textMemcachedClient.get("somekey");
        assertEquals(returnedValue, input);
    }

    @Test
    public void getsShouldComeWithACASValue() throws ExecutionException, InterruptedException {
        SomeValue input = new SomeValue(1, "world");
        OperationFuture<Boolean> success = textMemcachedClient.set("someKey2", 0, input);
        assertTrue(success.get());
        CASValue<Object> objectCASValue = textMemcachedClient.gets("someKey2");
        assertNotNull(objectCASValue.getCas());
        assertEquals(input, objectCASValue.getValue());
    }

    @Test
    public void shouldNotUpdateWhenCASvalueIsModified() throws InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        SomeValue input = new SomeValue(1, "world");
        textMemcachedClient.set("someKey3", 0, input);
        CASValue<Object> objectCASValue = textMemcachedClient.gets("someKey3");

        executorService.submit(() -> textMemcachedClient.set("someKey3", 0, new SomeValue(2, "world2")));
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        CASResponse cas = textMemcachedClient.cas("someKey3", objectCASValue.getCas(), "yolo");
        assertEquals(cas, CASResponse.EXISTS);
    }

    @Test
    public void deleteNonExistingKeyShouldReturnFalse() throws ExecutionException, InterruptedException {
        OperationFuture<Boolean> someOtherKey = textMemcachedClient.delete("someOtherKey");
        assertFalse(someOtherKey.get());
    }

    @Test
    public void deletingExistingKeyShouldReturnTrue() throws ExecutionException, InterruptedException {
        textMemcachedClient.set("someKey4", 0, "input");
        OperationFuture<Boolean> deletion = textMemcachedClient.delete("someKey4");
        assertTrue(deletion.get());
    }

    static class SomeValue implements Serializable {
        Integer integer;
        String string;

        public SomeValue(Integer integer, String string) {
            this.integer = integer;
            this.string = string;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SomeValue someValue = (SomeValue) o;

            if (integer != null ? !integer.equals(someValue.integer) : someValue.integer != null)
                return false;
            return !(string != null ? !string.equals(someValue.string) : someValue.string != null);

        }

        @Override
        public int hashCode() {
            int result = integer != null ? integer.hashCode() : 0;
            result = 31 * result + (string != null ? string.hashCode() : 0);
            return result;
        }
    }
    @AfterClass
    public static void shutdown() throws InterruptedException {
        textMemcachedClient.shutdown();
        thread.interrupt();
    }


}
