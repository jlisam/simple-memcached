package com.slack.memcached.protocol.text;


import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.LRUMemCache;
import com.slack.memcached.memory.MemCache;

import org.junit.Test;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;

import static org.junit.Assert.assertEquals;

public class TextServerHandlerTests {

    @Test
    public void shouldReturnValueAndEndWhenCacheHits() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);

        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));
        CachedValue value = new CachedValue(flags, payload);
        cache.set(key, value);
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new GetCommand(key));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("VALUE hello-world 1 7\r\n", Charset.defaultCharset())
                .writeBytes(payload)
                .writeBytes(Unpooled.copiedBuffer("\r\nEND\r\n", Charset.defaultCharset()));
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnENDWhenCacheMiss() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);

        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new GetCommand(key));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("END\r\n", Charset.defaultCharset());
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnUniqueIdWhenGets() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);

        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));

        CachedValue value = new CachedValue(flags, payload, 1231231313131L);
        cache.set(key, value);


        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new GetsCommand(key));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("VALUE hello-world 1 7 1231231313131\r\n", Charset.defaultCharset())
                .writeBytes(payload)
                .writeBytes(Unpooled.copiedBuffer("\r\nEND\r\n", Charset.defaultCharset()));
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnSTOREDWhenSet() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);
        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new SetCommand(key, flags, payload));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("STORED\r\n", Charset.defaultCharset());
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnDELETEDWhenDeleteSucceeds() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);
        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));
        CachedValue value = new CachedValue(flags, payload, 1231231313131L);
        cache.set(key, value);

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new DeleteCommand(key));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("DELETED\r\n", Charset.defaultCharset());
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnNOTFOUNDWhenDeleteDoesNotSucceed() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);
        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new DeleteCommand(key));
        embeddedChannel.finish();
        ByteBuf result = Unpooled.copiedBuffer("NOT_FOUND\r\n", Charset.defaultCharset());
        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnEXISTSWhenCasHasChanged() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);
        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));
        CachedValue value = new CachedValue(flags, payload, 1231231313131L);
        cache.set(key, value);

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new CasCommand(key, flags, 1231231313132L, payload));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("EXISTS\r\n", Charset.defaultCharset());

        assertEquals(embeddedChannel.readOutbound(), result);
    }

    @Test
    public void shouldReturnSTOREDWhenCasIdHasNotChanged() {
        MemCache<ByteBuf, CachedValue> cache = new LRUMemCache<>(1000);
        ByteBuf key = Unpooled.copiedBuffer("hello-world".getBytes(Charset.defaultCharset()));
        ByteBuf flags = Unpooled.copiedBuffer("1".getBytes(Charset.defaultCharset()));
        ByteBuf payload = Unpooled.copiedBuffer("payload".getBytes(Charset.defaultCharset()));
        CachedValue value = new CachedValue(flags, payload, 1231231313131L);
        cache.set(key, value);

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(new TextServerHandler(cache));
        embeddedChannel.writeInbound(new CasCommand(key, flags, 1231231313131L, payload));
        embeddedChannel.finish();

        ByteBuf result = Unpooled.copiedBuffer("STORED\r\n", Charset.defaultCharset());

        assertEquals(embeddedChannel.readOutbound(), result);
    }
}
