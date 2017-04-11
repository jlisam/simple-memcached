package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;
import com.slack.memcached.protocol.Command;

import java.nio.charset.Charset;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetCommand implements Command {
    static final ByteBuf GET = Unpooled.copiedBuffer("get", Charset.defaultCharset());
    private ByteBuf[] keys;

    public GetCommand(ByteBuf[] keys) {
        this.keys = new ByteBuf[keys.length];
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = keys[i].asReadOnly();
        }
    }

    public GetCommand(ByteBuf key) {
        this(new ByteBuf[]{key});
    }

    public ByteBuf[] getKeys() {
        return keys;
    }

    @Override
    public void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer) {

        ByteBuf response = Unpooled.buffer();
        for (int i = 0; i < keys.length; i++) {
            ByteBuf key = keys[i];

            if (cache.containsKey(key)) {
                CachedValue cachedValue = cache.get(key);
                ByteBuf value = cachedValue.getValue();
                ByteBuf flags = cachedValue.getFlags();
                response.writeBytes(VALUE)
                        .writeByte(' ')
                        .writeBytes(key)
                        .writeByte(' ')
                        .writeBytes(flags)
                        .writeByte(' ')
                        .writeBytes(String.valueOf(value.readableBytes()).getBytes(Charset.defaultCharset()))
                        .writeBytes(CRLF)
                        .writeBytes(value.slice().readerIndex(0))
                        .writeBytes(CRLF);
            }
        }

        response.writeBytes(END)
                .writeBytes(CRLF);
        consumer.accept(response);

    }
}
