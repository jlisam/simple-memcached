package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.CachedValue;
import com.slack.memcached.memory.MemCache;
import com.slack.memcached.protocol.Command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class TextServerHandler extends ChannelInboundHandlerAdapter {

    private MemCache<ByteBuf, CachedValue> cache;

    public TextServerHandler(MemCache<ByteBuf, CachedValue> cache) {
        this.cache = cache;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Command command = (Command) msg;
        command.process(cache, ctx::write);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ByteBuf error = Unpooled.buffer();
        error.writeBytes(Command.ERROR);
        error.writeBytes(Command.CRLF);
        ctx.writeAndFlush(error).awaitUninterruptibly();
        ctx.close();
    }

}