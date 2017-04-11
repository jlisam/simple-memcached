package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;
import com.slack.memcached.protocol.Command;

import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;

class DummyCommand implements Command {
    @Override
    public void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer) {
        // do nothing
    }
}
