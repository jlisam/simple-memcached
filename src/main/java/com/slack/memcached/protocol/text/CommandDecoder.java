package com.slack.memcached.protocol.text;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;


public class CommandDecoder extends ReplayingDecoder {

    private final AvailableCommands availableCommands = new AvailableCommands()
            .add(new GetCommandParser())
            .add(new SetCommandParser())
            .add(new GetsCommandParser())
            .add(new CasCommandParser())
            .add(new DeleteCommandParser());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.bytesBefore((byte) ' ');
        ByteBuf command = in.readBytes(length);
        availableCommands.tryParse(command, in).ifPresent(out::add);
    }

}
