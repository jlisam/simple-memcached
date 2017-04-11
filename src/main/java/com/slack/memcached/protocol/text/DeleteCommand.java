package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;
import com.slack.memcached.protocol.Command;

import java.nio.charset.Charset;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by jlisam on 4/10/17.
 */
public class DeleteCommand implements Command {

    static final ByteBuf DELETE = Unpooled.copiedBuffer("delete", Charset.defaultCharset());
    private final ByteBuf key;
    private final ByteBuf noReply;

    public DeleteCommand(ByteBuf key) {
        this(key, null);
    }

    public DeleteCommand(ByteBuf key, ByteBuf noReply) {
        this.key = key.asReadOnly();
        this.noReply = noReply;
    }

    @Override
    public void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer) {
        boolean success = cache.delete(key);
        ByteBuf response = Unpooled.buffer();
        if (success) {
            if (noReply == null) {
                response.writeBytes(DELETED)
                        .writeBytes(CRLF);
            } else {
                noReply.writeByte(CRLF[0]);
            }

        } else {
            if (noReply == null) {
                response.writeBytes(NOT_FOUND)
                        .writeBytes(CRLF);
            } else {
                response.writeByte(CRLF[0]);
            }
        }
        consumer.accept(response);
    }
}
