package com.slack.memcached.server;


import com.slack.memcached.memory.LRUMemCache;
import com.slack.memcached.protocol.text.CommandDecoder;
import com.slack.memcached.protocol.text.TextServerHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private final static int MAX_WORKER_THREADS = 12;
    private final static int BOSS_THREADS = 2;
    private String[] args;
    private ChannelFuture channelFuture;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;

    public void run(String[] args) throws Exception {
        this.args = args;
        runServer();
    }

    public void stop() throws InterruptedException {

        // Wait until the server socket is closed.
        // In this example, this does not happen, but you can do that to gracefully
        // shut down your server.
        if (channelFuture != null) {
            channelFuture.channel().closeFuture().sync();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private Options createOptions() {
        Options options = new Options();
        options.addOption("p", "port", true, "port number");
        options.addOption("c", "concurrency-level", true, "defaults to 16, number of buckets for cache");
        options.addOption("m", "max-entries", true, "max number of entries in cache");
        options.addOption("w", "worker-threads", true, "number of worker threads");
        options.addOption("b", "boss-threads", true, "number of server threads");
        return options;
    }

    private void runServer() throws Exception {
        CommandLineParser commandLineParser = new DefaultParser();
        Options options = createOptions();
        try {
            CommandLine commandLine = commandLineParser.parse(options, args, false);
            int port;
            int maxEntries;
            int concurrencyLevel;
            int workerThreads;
            int bossThreads;

            if (commandLine.hasOption('p')) {
                port = Integer.parseInt(commandLine.getOptionValue('p'));
            } else {
                port = 11211;
            }

            if (commandLine.hasOption('c')) {
                concurrencyLevel = Integer.parseInt(commandLine.getOptionValue('c'));
            } else {
                concurrencyLevel = 16;
            }

            if (commandLine.hasOption('m')) {
                maxEntries = Integer.parseInt(commandLine.getOptionValue('m'));
            } else {
                maxEntries = 100000;
            }

            if (commandLine.hasOption('w')) {
                workerThreads = Integer.parseInt(commandLine.getOptionValue('w'));
            } else {
                workerThreads = calculateThreadCount();
            }

            if (commandLine.hasOption('b')) {
                bossThreads = Integer.parseInt(commandLine.getOptionValue('b'));
            } else {
                bossThreads = BOSS_THREADS;
            }
            run(port, bossThreads, workerThreads, maxEntries, concurrencyLevel);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("memcached", options);
            System.exit(2);
        }
    }

    private int calculateThreadCount() {
        int threadCount;
        if ((threadCount = SystemPropertyUtil.getInt("io.netty.eventLoopThreads", 0)) > 0) {
            return threadCount;
        } else {
            threadCount = Runtime.getRuntime().availableProcessors() * 2;
            return threadCount > MAX_WORKER_THREADS ? MAX_WORKER_THREADS : threadCount;
        }
    }

    private void run(int port, int bossThreads, int workerThreads, int maxEntries, int concurrencyLevel) throws Exception {
        bossGroup = new NioEventLoopGroup(bossThreads);
        workerGroup = new NioEventLoopGroup(workerThreads);
        TextServerHandler textServerHandler = new TextServerHandler(new LRUMemCache<>(maxEntries, concurrencyLevel));

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new CommandDecoder())
                                .addLast(textServerHandler);
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        channelFuture = b.bind(port).sync();

    }


}
