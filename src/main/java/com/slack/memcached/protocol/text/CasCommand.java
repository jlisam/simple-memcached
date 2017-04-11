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

/**
 * Created by jlisam on 4/10/17.
 */
public class CasCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(CasCommand.class);

    static final ByteBuf CAS = Unpooled.copiedBuffer("cas", Charset.defaultCharset());
    private final ByteBuf key;
    private final long cas;
    private final ByteBuf flags;
    private final ByteBuf noReply;
    private final ByteBuf payload;


    public CasCommand(ByteBuf key, ByteBuf flags, long cas, ByteBuf payload) {
        this(key, flags, cas, payload, null);
    }

    public CasCommand(ByteBuf key, ByteBuf flags, long cas, ByteBuf payload, ByteBuf noReply) {
        this.key = key;
        this.flags = flags;
        this.cas = cas;
        this.payload = payload;
        this.noReply = noReply;
    }

    @Override
    public void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer) {

        CachedValue newValue = new CachedValue(flags, payload, getUniqueTimestamp());
        Boolean result = cache.cas(key, newValue, (x) -> x.getCas() == cas);
        ByteBuf response = Unpooled.buffer();
        if (noReply == null) {
            if (result == null) {
                response.writeBytes(NOT_FOUND);
                response.writeBytes(CRLF);
            } else if (result) {
                response.writeBytes(STORED);
                response.writeBytes(CRLF);
            } else {
                response.writeBytes(EXISTS);
                response.writeBytes(CRLF);
            }
        } else {
            response.writeByte(CRLF[0]);
        }
        consumer.accept(response);
    }
}
