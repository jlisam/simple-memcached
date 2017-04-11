package com.slack.memcached.protocol;


import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;

import java.nio.charset.Charset;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;

public interface Command {

    byte[] CRLF = new byte[]{'\r', '\n'};
    byte[] VALUE = "VALUE".getBytes(Charset.defaultCharset());
    byte[] END = "END".getBytes(Charset.defaultCharset());
    byte[] STORED = "STORED".getBytes(Charset.defaultCharset());
    byte[] DELETED = "DELETED".getBytes(Charset.defaultCharset());
    byte[] NOT_FOUND = "NOT_FOUND".getBytes(Charset.defaultCharset());
    byte[] ERROR = "ERROR".getBytes(Charset.defaultCharset());
    byte[] CLIENT_ERROR = "CLIENT_ERROR".getBytes(Charset.defaultCharset());
    byte[] BAD_DATA_CHUNK = "bad data chunk".getBytes(Charset.defaultCharset());
    byte[] EXISTS = "EXISTS".getBytes(Charset.defaultCharset());

    void process(MemCache<ByteBuf, CachedValue> cache, Consumer<ByteBuf> consumer);
}
