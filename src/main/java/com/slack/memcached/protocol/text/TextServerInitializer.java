package com.slack.memcached.protocol.text;

import com.slack.memcached.memory.LRUMemCache;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Creates a newly configured {@link ChannelPipeline} for a new channel.
 */
public class TextServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new CommandDecoder());

        // and then business logic.
        pipeline.addLast(new TextServerHandler(new LRUMemCache<>(10000)));
    }
}