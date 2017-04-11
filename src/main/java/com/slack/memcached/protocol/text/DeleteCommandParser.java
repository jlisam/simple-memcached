package com.slack.memcached.protocol.text;

import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static com.slack.memcached.protocol.Command.CRLF;
import static com.slack.memcached.protocol.text.DeleteCommand.DELETE;

public class DeleteCommandParser implements CommandParser {
    @Override
    public Command parse(ByteBuf rest) {
        rest.skipBytes(1);

        ByteBuf key = rest.readRetainedSlice(rest.bytesBefore((byte) '\r'));
        rest.readerIndex(rest.readerIndex() + CRLF.length);

        String[] tokens = key.toString(Charset.defaultCharset()).split(" ");

        ByteBuf noReply = null;
        if (tokens.length > 1) {
            key = Unpooled.copiedBuffer(tokens[0], Charset.defaultCharset());
            noReply = Unpooled.copiedBuffer(tokens[1], Charset.defaultCharset());
        }
        return new DeleteCommand(key, noReply);
    }

    @Override
    public ByteBuf getCommand() {
        return DELETE;
    }
}
