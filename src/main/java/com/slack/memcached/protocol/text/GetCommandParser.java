package com.slack.memcached.protocol.text;

import com.slack.memcached.protocol.Command;
import com.slack.memcached.protocol.CommandParser;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class GetCommandParser implements CommandParser {

    @Override
    public Command parse(ByteBuf rest) {
        rest.skipBytes(1);
        ByteBuf key = rest.readRetainedSlice(rest.bytesBefore((byte) '\r'));
        String[] keyTokens = key.toString(Charset.defaultCharset()).split(" ");

        rest.readerIndex(rest.readerIndex() + Command.CRLF.length);
        return new GetCommand(convertToByteBufArr(keyTokens));
    }

    @Override
    public ByteBuf getCommand() {
        return GetCommand.GET;
    }

    // hack
    private ByteBuf[] convertToByteBufArr(String[] strings){
        ByteBuf[] result = new ByteBuf[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = Unpooled.wrappedBuffer(strings[i].getBytes(Charset.defaultCharset()));
        }
        return  result;
    }
}
