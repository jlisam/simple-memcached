package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;
import com.slack.memcached.protocol.Command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.slack.memcached.util.UniqueIdGenerator.getUniqueTimestamp;

public class SetCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(SetCommand.class);

    static final ByteBuf SET = Unpooled.copiedBuffer("set", Charset.defaultCharset());
    private final ByteBuf key;
    private final ByteBuf flags;
    private final ByteBuf payload;
    private final ByteBuf noReply;


    public SetCommand(ByteBuf key, ByteBuf flags, ByteBuf payload) {
        this(key, flags, payload, null);
    }

    public SetCommand(ByteBuf key, ByteBuf flags, ByteBuf payload, ByteBuf noReply) {
        this.key = key;
        this.flags = flags;
        this.payload = payload;
        this.noReply = noReply;
    }

    @Override
    public void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer) {
        CachedValue cachedValue = new CachedValue(flags, payload, getUniqueTimestamp());
        cache.set(key, cachedValue);

        if (noReply == null) {
            consumer.accept(Unpooled.buffer()
                    .writeBytes(STORED)
                    .writeBytes(CRLF));
        } else {
            consumer.accept(Unpooled.buffer()
                    .writeByte(CRLF[0]));
        }

    }

    ByteBuf getKey() {
        return key;
    }

    ByteBuf getFlags() {
        return flags;
    }

    ByteBuf getPayload() {
        return payload;
    }
}
