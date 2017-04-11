package com.slack.memcached.protocol.text;

import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import io.netty.buffer.ByteBuf;


public class DummyCommandParser implements CommandParser {
    @Override
    public Command parse(ByteBuf rest) {
        rest.skipBytes(rest.readableBytes());
        return null;
    }

    @Override
    public ByteBuf getCommand() {
        return null;
    }
}
