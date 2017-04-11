package com.slack.memcached.protocol;

import com.slack.memcached.exception.BadDataChunkException;

import io.netty.buffer.ByteBuf;

/**
 * Created by jlisam on 4/10/17.
 */
public interface CommandParser {

    Command parse(ByteBuf rest) throws BadDataChunkException;

    ByteBuf getCommand();
}
